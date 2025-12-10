package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NodeCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(NodeCalculator.class);

    private static final String NODE_CABINET_FIXING = "NODE_CABINET_FIXING";
    private static final String NODE_INPUT_GLAND = "NODE_INPUT_GLAND";
    private static final String NODE_LUGS = "NODE_LUGS";
    private static final String NODE_CIRCUIT_BREAKER = "NODE_CIRCUIT_BREAKER";
    private static final String NODE_SOCKET_DOUBLE = "NODE_SOCKET_DOUBLE";
    private static final String CABINET_PREFIX = "NODE_CABINET";

    private static final int MIN_LUGS = 10;
    private static final int LUGS_PER_EXTRA_SOCKET = 4;

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public NodeCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForNode(
            NetworkNode node, int incomingLinesCount, CalculationSettingsDto settings) {
        Map<Material, Double> result = new HashMap<>();
        if (node == null) {
            LOG.warn("Network node is null, skipping calculation");
            return result;
        }

        addCabinetFixing(node.getMountSurface(), result);
        addIncomingGlands(incomingLinesCount, result);
        addLugs(node.getExtraSockets(), result);
        addSockets(node.getBaseSockets(), node.getExtraSockets(), result);
        addCircuitBreakers(node.getBaseCircuitBreakers(), node.getExtraCircuitBreakers(), result);
        addCabinetMaterial(node.getCabinetSize(), settings, result);

        return result;
    }

    private void addCabinetFixing(String mountSurface, Map<Material, Double> result) {
        String contextType = NODE_CABINET_FIXING;
        if (StringUtils.hasText(mountSurface)) {
            contextType += "_" + mountSurface.trim().toUpperCase(Locale.ROOT);
        }
        addFromNorm(result, contextType, Map.of("deviceCount", 1));
    }

    private void addIncomingGlands(int incomingLinesCount, Map<Material, Double> result) {
        if (incomingLinesCount <= 0) {
            return;
        }
        addFromNorm(result, NODE_INPUT_GLAND, Map.of("incomingLinesCount", incomingLinesCount));
    }

    private void addLugs(Integer extraSockets, Map<Material, Double> result) {
        int extra = extraSockets != null ? extraSockets : 0;
        int quantity = Math.max(MIN_LUGS, MIN_LUGS + (extra * LUGS_PER_EXTRA_SOCKET));
        addFromNorm(result, NODE_LUGS, Map.of("lugCount", quantity, "extraSockets", extra));
    }

    private void addSockets(Integer baseSockets, Integer extraSockets, Map<Material, Double> result) {
        int base = baseSockets != null ? baseSockets : 0;
        int extra = extraSockets != null ? extraSockets : 0;
        int total = base + extra;
        if (total > 0) {
            Map<String, Object> context = new HashMap<>();
            context.put("socketCount", total);
            context.put("baseSockets", base);
            context.put("extraSockets", extra);
            addFromNorm(result, NODE_SOCKET_DOUBLE, context);
        }
    }

    private void addCircuitBreakers(Integer baseCircuitBreakers, Integer extraCircuitBreakers, Map<Material, Double> result) {
        int base = baseCircuitBreakers != null ? baseCircuitBreakers : 0;
        int extra = extraCircuitBreakers != null ? extraCircuitBreakers : 0;
        if (base + extra > 0) {
            Map<String, Object> context = new HashMap<>();
            context.put("count", base + extra);
            context.put("baseBreakers", base);
            context.put("extraBreakers", extra);
            addFromNorm(result, NODE_CIRCUIT_BREAKER, context);
        }
    }

    private void addCabinetMaterial(Integer cabinetSize, CalculationSettingsDto settings, Map<Material, Double> result) {
        if (cabinetSize == null) {
            LOG.warn("Cabinet size is not specified, skipping cabinet materials");
            return;
        }
        int size = cabinetSize;
        Map<String, Object> context = new HashMap<>();
        context.put("cabinetSize", size);
        if (settings != null && settings.getStandardCabinetDropLengthMeters() != null) {
            context.put("dropLength", settings.getStandardCabinetDropLengthMeters());
        }
        String contextType = CABINET_PREFIX + "_" + size;
        addFromNorm(result, contextType, context);
    }

    private void addFromNorm(Map<Material, Double> result, String contextType, Map<String, Object> context) {
        List<MaterialNorm> norms = materialNormRepository.findAllByContextType(contextType);
        if (norms.isEmpty()) {
            LOG.warn("Material norm not found for context: {}", contextType);
            return;
        }

        for (MaterialNorm norm : norms) {
            double quantity = expressionEvaluator.evaluate(norm.getFormula(), context);
            if (quantity > 0) {
                result.merge(norm.getMaterial(), quantity, Double::sum);
            }
        }
    }
}
