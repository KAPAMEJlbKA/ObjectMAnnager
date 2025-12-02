package com.kapamejlbka.objectmanager.domain.calcengine.routes;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RouteCalculator {

    private static final String CORRUGATED_PIPE_HORIZONTAL = "CORRUGATED_PIPE_HORIZONTAL";
    private static final String CORRUGATED_PIPE_VERTICAL = "CORRUGATED_PIPE_VERTICAL";
    private static final String CORRUGATED_PIPE_COUPLING = "CORRUGATED_PIPE_COUPLING";
    private static final String CORRUGATED_PIPE_BRANCH = "CORRUGATED_PIPE_BRANCH";
    private static final String CABLE_CHANNEL_PREFIX = "CABLE_CHANNEL";
    private static final String TRAY_OR_STRUCTURE_TIES = "TRAY_OR_STRUCTURE_TIES";
    private static final String WIRE_ROPE_TIES = "WIRE_ROPE_TIES";
    private static final String BARE_CABLE_ONE_CLIP = "BARE_CABLE_ONE_CLIP";
    private static final String BARE_CABLE_PE_TIES = "BARE_CABLE_PE_TIES";

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public RouteCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForRoute(InstallationRoute route, List<TopologyLink> linksInRoute) {
        Objects.requireNonNull(route, "Installation route is required");
        double lengthMeters = Objects.requireNonNull(route.getLengthMeters(), "Route length is required");
        String routeType = Objects.requireNonNull(route.getRouteType(), "Route type is required").toUpperCase();
        List<TopologyLink> links = linksInRoute == null ? List.of() : linksInRoute;

        Map<Material, Double> result = new HashMap<>();

        switch (routeType) {
            case "CORRUGATED_PIPE" -> handleCorrugatedPipe(route, lengthMeters, links, result);
            case "CABLE_CHANNEL" -> handleCableChannel(route, lengthMeters, result);
            case "TRAY_OR_STRUCTURE" -> handleTrayOrStructure(lengthMeters, result);
            case "WIRE_ROPE" -> handleWireRope(lengthMeters, result);
            case "BARE_CABLE" -> handleBareCable(route, lengthMeters, result);
            default -> throw new IllegalArgumentException("Unsupported route type: " + routeType);
        }

        return result;
    }

    private void handleCorrugatedPipe(
            InstallationRoute route, double lengthMeters, List<TopologyLink> links, Map<Material, Double> result) {
        String orientation = Objects.requireNonNull(route.getOrientation(), "Orientation is required for corrugated pipe")
                .toUpperCase();

        if ("HORIZONTAL".equals(orientation)) {
            addFromNorm(result, CORRUGATED_PIPE_HORIZONTAL, Map.of("lengthMeters", lengthMeters, "step", 0.4));
        } else if ("VERTICAL".equals(orientation)) {
            addFromNorm(result, CORRUGATED_PIPE_VERTICAL, Map.of("lengthMeters", lengthMeters, "step", 0.5));
        } else {
            throw new IllegalArgumentException("Unsupported corrugated pipe orientation: " + orientation);
        }

        if (lengthMeters > 100) {
            addFromNorm(result, CORRUGATED_PIPE_COUPLING, Map.of("lengthMeters", lengthMeters));
        }

        int branchCount = links.size();
        if (branchCount > 0) {
            addFromNorm(result, CORRUGATED_PIPE_BRANCH, Map.of("branches", branchCount));
        }
    }

    private void handleCableChannel(InstallationRoute route, double lengthMeters, Map<Material, Double> result) {
        String mountSurface = route.getMountSurface();
        String contextType = CABLE_CHANNEL_PREFIX;
        if (mountSurface != null && !mountSurface.isBlank()) {
            contextType = CABLE_CHANNEL_PREFIX + "_" + mountSurface.toUpperCase();
        }
        addFromNorm(result, contextType, Map.of("lengthMeters", lengthMeters, "step", 0.4));
    }

    private void handleTrayOrStructure(double lengthMeters, Map<Material, Double> result) {
        addFromNorm(result, TRAY_OR_STRUCTURE_TIES, Map.of("lengthMeters", lengthMeters, "step", 0.5));
    }

    private void handleWireRope(double lengthMeters, Map<Material, Double> result) {
        addFromNorm(result, WIRE_ROPE_TIES, Map.of("lengthMeters", lengthMeters, "step", 0.3));
    }

    private void handleBareCable(InstallationRoute route, double lengthMeters, Map<Material, Double> result) {
        String fixingMethod = Objects.requireNonNull(route.getFixingMethod(), "Fixing method is required for bare cable")
                .toUpperCase();
        String contextType = switch (fixingMethod) {
            case "ONE_CLIP" -> BARE_CABLE_ONE_CLIP;
            case "PE_TIES" -> BARE_CABLE_PE_TIES;
            default -> throw new IllegalArgumentException("Unsupported bare cable fixing method: " + fixingMethod);
        };
        addFromNorm(result, contextType, Map.of("lengthMeters", lengthMeters, "step", 0.4));
    }

    private void addFromNorm(Map<Material, Double> result, String contextType, Map<String, Object> context) {
        MaterialNorm norm = materialNormRepository
                .findByContextType(contextType)
                .orElseThrow(() -> new IllegalArgumentException("Material norm not found for context: " + contextType));
        double quantity = expressionEvaluator.evaluate(norm.getFormula(), context);
        if (quantity > 0) {
            result.merge(norm.getMaterial(), quantity, Double::sum);
        }
    }
}
