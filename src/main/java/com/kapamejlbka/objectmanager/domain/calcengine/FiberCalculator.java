package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FiberCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(FiberCalculator.class);

    private static final String FIBER_CABLE_TEMPLATE = "FIBER_%s";
    private static final String FIBER_SPLICE = "FIBER_SPLICE";
    private static final String FIBER_CONNECTOR = "FIBER_CONNECTOR";

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public FiberCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForFiberLink(TopologyLink fiberLink) {
        Map<Material, Double> result = new HashMap<>();
        if (fiberLink == null) {
            LOG.warn("Fiber link is null, skipping calculation");
            return result;
        }

        Double cableLength = fiberLink.getCableLength();
        Integer fiberCores = fiberLink.getFiberCores();
        if (cableLength == null || fiberCores == null) {
            LOG.warn("Fiber link data is incomplete for link {}", fiberLink.getId());
            return result;
        }

        Integer fiberSpliceCount = defaultZero(fiberLink.getFiberSpliceCount());
        Integer fiberConnectorCount = defaultZero(fiberLink.getFiberConnectorCount());

        addFromNorm(result, FIBER_CABLE_TEMPLATE.formatted(fiberCores), Map.of("length", cableLength));

        if (fiberSpliceCount > 0) {
            addFromNorm(result, FIBER_SPLICE, Map.of("fiberSpliceCount", fiberSpliceCount));
        }

        if (fiberConnectorCount > 0) {
            addFromNorm(result, FIBER_CONNECTOR, Map.of("fiberConnectorCount", fiberConnectorCount));
        }

        return result;
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
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
