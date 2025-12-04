package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NodeCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(NodeCalculator.class);

    private static final String ENDPOINT_MOUNT_PREFIX = "ENDPOINT_MOUNT";
    private static final String INCOMING_COUPLING = "NODE_INCOMING_COUPLING";
    private static final String TERMINAL = "NODE_TERMINAL";
    private static final String CIRCUIT_BREAKER = "NODE_CIRCUIT_BREAKER";
    private static final String CABINET_PREFIX = "NODE_CABINET";

    private static final int CABINET_MOUNT_POINTS = 4;
    private static final int MIN_TERMINALS = 10;
    private static final int TERMINALS_PER_EXTRA_SOCKET = 4;

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public NodeCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculate(NetworkNode node) {
        Objects.requireNonNull(node, "Network node is required");

        Map<Material, Double> result = new HashMap<>();

        addMountMaterials(node.getMountSurface(), result);
        addIncomingCouplings(node.getIncomingLinesCount(), result);
        addTerminals(node.getExtraSockets(), result);
        addCircuitBreakers(node.getBaseCircuitBreakers(), node.getExtraCircuitBreakers(), result);
        addCabinetMaterial(node.getCabinetSize(), result);

        return result;
    }

    private void addMountMaterials(String mountSurface, Map<Material, Double> result) {
        String contextType = ENDPOINT_MOUNT_PREFIX;
        if (StringUtils.hasText(mountSurface)) {
            contextType += "_" + mountSurface.trim().toUpperCase(Locale.ROOT);
        }
        addFromNorm(result, contextType, Map.of("points", CABINET_MOUNT_POINTS));
    }

    private void addIncomingCouplings(Integer incomingLinesCount, Map<Material, Double> result) {
        if (incomingLinesCount == null || incomingLinesCount <= 0) {
            return;
        }
        addFromNorm(result, INCOMING_COUPLING, Map.of("count", incomingLinesCount));
    }

    private void addTerminals(Integer extraSockets, Map<Material, Double> result) {
        int extra = extraSockets != null ? extraSockets : 0;
        int quantity = Math.max(MIN_TERMINALS, MIN_TERMINALS + (extra * TERMINALS_PER_EXTRA_SOCKET));
        addFromNorm(result, TERMINAL, Map.of("count", quantity));
    }

    private void addCircuitBreakers(Integer baseCircuitBreakers, Integer extraCircuitBreakers, Map<Material, Double> result) {
        int base = Objects.requireNonNull(baseCircuitBreakers, "Base circuit breakers are required");
        int extra = extraCircuitBreakers != null ? extraCircuitBreakers : 0;
        addFromNorm(result, CIRCUIT_BREAKER, Map.of("count", base + extra));
    }

    private void addCabinetMaterial(Integer cabinetSize, Map<Material, Double> result) {
        int size = Objects.requireNonNull(cabinetSize, "Cabinet size is required");
        String contextType = CABINET_PREFIX + "_" + size;
        addFromNorm(result, contextType, Map.of("cabinetSize", size));
    }

    private void addFromNorm(Map<Material, Double> result, String contextType, Map<String, Object> context) {
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
}
