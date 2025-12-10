package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(LinkCalculator.class);

    private static final String LINK_UTP_LENGTH = "LINK_UTP_LENGTH";
    private static final String LINK_POWER_LENGTH = "LINK_POWER_LENGTH";

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public LinkCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForLink(TopologyLink link) {
        Map<Material, Double> result = new HashMap<>();
        if (link == null) {
            LOG.warn("Topology link is null, skipping calculation");
            return result;
        }

        String linkType = normalize(link.getLinkType());
        if (linkType.isEmpty()) {
            LOG.warn("Topology link type is missing for link {}", link.getId());
            return result;
        }

        if (Boolean.TRUE.equals(link.getWireless())) {
            return result;
        }

        Double length = link.getCableLength();
        if (length == null || length <= 0) {
            LOG.warn("Cable length is missing for link {}", link.getId());
            return result;
        }

        switch (linkType) {
            case "UTP" -> addFromNorm(result, LINK_UTP_LENGTH, baseContext(length));
            case "POWER" -> addFromNorm(result, LINK_POWER_LENGTH, baseContext(length));
            case "FIBER" -> {
                // Handled by FiberCalculator
            }
            default -> LOG.warn("Unsupported topology link type: {}", linkType);
        }

        return result;
    }

    private Map<String, Object> baseContext(Double length) {
        Map<String, Object> context = new HashMap<>();
        context.put("length", length);
        context.put("lengthMeters", length);
        return context;
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

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
