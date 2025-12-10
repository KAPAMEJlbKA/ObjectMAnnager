package com.kapamejlbka.objectmanager.domain.calcengine.routes;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RouteCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(RouteCalculator.class);

    private static final String CORRUGATED_PIPE_HORIZONTAL_CLIP = "CORRUGATED_PIPE_HORIZONTAL_CLIP";
    private static final String CORRUGATED_PIPE_VERTICAL_CLIP = "CORRUGATED_PIPE_VERTICAL_CLIP";
    private static final String CORRUGATED_PIPE_COUPLING = "CORRUGATED_PIPE_COUPLING";
    private static final String CORRUGATED_PIPE_BRANCH = "CORRUGATED_PIPE_BRANCH";
    private static final String CABLE_CHANNEL_CLIP = "CABLE_CHANNEL_CLIP";
    private static final String CABLE_CHANNEL_FASTENER = "CABLE_CHANNEL_FASTENER";
    private static final String TRAY_OR_STRUCTURE_TIES = "CABLE_TIE_ON_TRAY";
    private static final String WIRE_ROPE_ANCHOR = "WIRE_ROPE_ANCHOR";
    private static final String WIRE_ROPE_TURNBUCKLE = "WIRE_ROPE_TURNBUCKLE";
    private static final String WIRE_ROPE_CLAMP = "WIRE_ROPE_CLAMP";
    private static final String BARE_CABLE_ONE_CLIP = "BARE_CABLE_ONE_CLIP";
    private static final String BARE_CABLE_PE_TIES = "BARE_CABLE_PE_TIES";

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public RouteCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForRoute(
            InstallationRoute route, List<TopologyLink> linksInRoute, CalculationSettingsDto settings) {
        Map<Material, Double> result = new HashMap<>();
        if (route == null) {
            LOG.warn("Route is null, skipping calculation");
            return result;
        }

        Double lengthMeters = route.getLengthMeters();
        String routeType = normalize(route.getRouteType());
        if (lengthMeters == null || lengthMeters <= 0 || routeType.isEmpty()) {
            LOG.warn("Route data is incomplete for route {}", route.getName());
            return result;
        }

        List<TopologyLink> links = linksInRoute == null ? List.of() : linksInRoute;

        switch (routeType) {
            case "CORRUGATED_PIPE" -> handleCorrugatedPipe(route, lengthMeters, links, settings, result);
            case "CABLE_CHANNEL" -> handleCableChannel(route, lengthMeters, result);
            case "TRAY_OR_STRUCTURE" -> handleTrayOrStructure(lengthMeters, result);
            case "WIRE_ROPE" -> handleWireRope(lengthMeters, result);
            case "BARE_CABLE" -> handleBareCable(route, lengthMeters, result);
            default -> LOG.warn("Unsupported route type: {}", routeType);
        }

        return result;
    }

    private void handleCorrugatedPipe(
            InstallationRoute route,
            double lengthMeters,
            List<TopologyLink> links,
            CalculationSettingsDto settings,
            Map<Material, Double> result) {
        String orientation = normalize(route.getOrientation());
        if (orientation.isEmpty()) {
            LOG.warn("Orientation is missing for corrugated pipe route {}", route.getName());
            return;
        }

        double horizontalStep = Optional.ofNullable(settings).map(CalculationSettingsDto::getDefaultHorizontalClipStep).orElse(0.4);
        double verticalStep = Optional.ofNullable(settings).map(CalculationSettingsDto::getDefaultVerticalClipStep).orElse(0.5);

        if ("HORIZONTAL".equals(orientation)) {
            addFromNorm(result, CORRUGATED_PIPE_HORIZONTAL_CLIP, baseContext(lengthMeters, horizontalStep));
        } else if ("VERTICAL".equals(orientation)) {
            addFromNorm(result, CORRUGATED_PIPE_VERTICAL_CLIP, baseContext(lengthMeters, verticalStep));
        } else {
            LOG.warn("Unsupported corrugated pipe orientation: {}", orientation);
        }

        addFromNorm(result, CORRUGATED_PIPE_COUPLING, Map.of("length", lengthMeters));

        int branchCount = links.size();
        if (branchCount > 0) {
            addFromNorm(result, CORRUGATED_PIPE_BRANCH, Map.of("branchCount", branchCount));
        }

        String mountSurface = normalize(route.getMountSurface());
        if (!mountSurface.isEmpty()) {
            addFromNorm(
                    result,
                    CORRUGATED_PIPE_HORIZONTAL_CLIP + "_" + mountSurface,
                    baseContext(lengthMeters, horizontalStep));
        }
    }

    private void handleCableChannel(InstallationRoute route, double lengthMeters, Map<Material, Double> result) {
        String mountSurface = normalize(route.getMountSurface());
        addFromNorm(result, CABLE_CHANNEL_CLIP, baseContext(lengthMeters, 0.4));
        addFromNorm(result, CABLE_CHANNEL_FASTENER, baseContext(lengthMeters, 0.4));
        if (!mountSurface.isEmpty()) {
            addFromNorm(result, CABLE_CHANNEL_FASTENER + "_" + mountSurface, baseContext(lengthMeters, 0.4));
        }
    }

    private void handleTrayOrStructure(double lengthMeters, Map<Material, Double> result) {
        addFromNorm(result, TRAY_OR_STRUCTURE_TIES, baseContext(lengthMeters, 0.5));
    }

    private void handleWireRope(double lengthMeters, Map<Material, Double> result) {
        Map<String, Object> context = baseContext(lengthMeters, 0.3);
        addFromNorm(result, WIRE_ROPE_ANCHOR, context);
        addFromNorm(result, WIRE_ROPE_TURNBUCKLE, context);
        addFromNorm(result, WIRE_ROPE_CLAMP, context);
    }

    private void handleBareCable(InstallationRoute route, double lengthMeters, Map<Material, Double> result) {
        String fixingMethod = normalize(route.getFixingMethod());
        if (fixingMethod.isEmpty()) {
            LOG.warn("Fixing method is missing for bare cable route {}", route.getName());
            return;
        }
        String contextType = switch (fixingMethod) {
            case "ONE_CLIP" -> BARE_CABLE_ONE_CLIP;
            case "PE_TIES" -> BARE_CABLE_PE_TIES;
            default -> {
                LOG.warn("Unsupported bare cable fixing method: {}", fixingMethod);
                yield "";
            }
        };
        if (!contextType.isEmpty()) {
            addFromNorm(result, contextType, baseContext(lengthMeters, 0.4));
        }
    }

    private Map<String, Object> baseContext(double lengthMeters, double step) {
        Map<String, Object> context = new HashMap<>();
        context.put("length", lengthMeters);
        context.put("lengthMeters", lengthMeters);
        context.put("step", step);
        return context;
    }

    private void addFromNorm(Map<Material, Double> result, String contextType, Map<String, Object> context) {
        if (contextType == null || contextType.isBlank()) {
            return;
        }

        MaterialNorm norm = materialNormRepository
                .findByContextType(contextType)
                .orElseGet(() -> {
                    LOG.warn("Material norm not found for context: {}", contextType);
                    return null;
                });

        if (norm == null) {
            return;
        }

        double quantity = expressionEvaluator.evaluate(norm.getFormula(), context);
        if (quantity > 0) {
            result.merge(norm.getMaterial(), quantity, Double::sum);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
