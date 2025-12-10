package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
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
public class EndpointCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointCalculator.class);

    private static final String CAMERA = "CAMERA";
    private static final String ACCESS_POINT = "ACCESS_POINT";
    private static final String NETWORK_OUTLET = "NETWORK_OUTLET";
    private static final String READER = "READER";
    private static final String TURNSTILE = "TURNSTILE";
    private static final String OTHER_NETWORK_DEVICE = "OTHER_NETWORK_DEVICE";

    private static final String ENDPOINT_CAMERA_FIXING = "ENDPOINT_CAMERA_FIXING";
    private static final String ENDPOINT_CAMERA_RJ45 = "ENDPOINT_CAMERA_RJ45";
    private static final String ENDPOINT_ACCESS_POINT_FIXING = "ENDPOINT_ACCESS_POINT_FIXING";
    private static final String ENDPOINT_ACCESS_POINT_RJ45 = "ENDPOINT_ACCESS_POINT_RJ45";
    private static final String ENDPOINT_READER_FIXING = "ENDPOINT_READER_FIXING";
    private static final String ENDPOINT_READER_RJ45 = "ENDPOINT_READER_RJ45";
    private static final String ENDPOINT_NETWORK_OUTLET_RJ45 = "ENDPOINT_NETWORK_OUTLET_RJ45";
    private static final String ENDPOINT_NETWORK_OUTLET_FIXING = "ENDPOINT_NETWORK_OUTLET_FIXING";
    private static final String ENDPOINT_TURNSTILE_FIXING = "ENDPOINT_TURNSTILE_FIXING";
    private static final String ENDPOINT_OTHER_DEVICE_FIXING = "ENDPOINT_OTHER_NETWORK_DEVICE_FIXING";
    private static final String ENDPOINT_OTHER_DEVICE_RJ45 = "ENDPOINT_OTHER_NETWORK_DEVICE_RJ45";

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public EndpointCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculateForDevice(EndpointDevice device) {
        Map<Material, Double> result = new HashMap<>();
        if (device == null) {
            LOG.warn("Endpoint device is null, skipping calculation");
            return result;
        }

        String normalizedType = normalizeType(device.getType());
        if (normalizedType.isEmpty()) {
            LOG.warn("Endpoint device type is missing for device {}", device.getName());
            return result;
        }

        switch (normalizedType) {
            case CAMERA -> {
                addFromNorm(result, ENDPOINT_CAMERA_FIXING, Map.of("deviceCount", 1));
                addFromNorm(result, ENDPOINT_CAMERA_RJ45, Map.of("deviceCount", 1));
            }
            case ACCESS_POINT -> {
                addFromNorm(result, ENDPOINT_ACCESS_POINT_FIXING, Map.of("deviceCount", 1));
                addFromNorm(result, ENDPOINT_ACCESS_POINT_RJ45, Map.of("deviceCount", 1));
            }
            case NETWORK_OUTLET -> {
                addFromNorm(result, ENDPOINT_NETWORK_OUTLET_FIXING, Map.of("deviceCount", 1));
                addFromNorm(result, ENDPOINT_NETWORK_OUTLET_RJ45, Map.of("deviceCount", 1));
            }
            case READER -> {
                addFromNorm(result, ENDPOINT_READER_FIXING, Map.of("deviceCount", 1));
                addFromNorm(result, ENDPOINT_READER_RJ45, Map.of("deviceCount", 1));
            }
            case TURNSTILE -> addFromNorm(result, ENDPOINT_TURNSTILE_FIXING, Map.of("deviceCount", 1));
            case OTHER_NETWORK_DEVICE -> {
                addFromNorm(result, ENDPOINT_OTHER_DEVICE_FIXING, Map.of("deviceCount", 1));
                addFromNorm(result, ENDPOINT_OTHER_DEVICE_RJ45, Map.of("deviceCount", 1));
            }
            default -> LOG.warn("Unsupported endpoint device type: {}", normalizedType);
        }

        return result;
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

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
