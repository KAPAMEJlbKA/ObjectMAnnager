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
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EndpointCalculator {

    private static final String ENDPOINT_MOUNT_PREFIX = "ENDPOINT_MOUNT";
    private static final String RJ45_CONNECTOR = "RJ45_CONNECTOR";
    private static final String TURNSTILE_ANCHOR = "TURNSTILE_ANCHOR";

    private static final int CAMERA_MOUNT_POINTS = 4;
    private static final int CAMERA_RJ45_COUNT = 2;
    private static final int ACCESS_POINT_MOUNT_POINTS = 4;
    private static final int ACCESS_POINT_RJ45_COUNT = 4;
    private static final int NETWORK_DEVICE_RJ45_COUNT = 2;
    private static final int TURNSTILE_ANCHORS = 4;

    private static final List<String> CAMERA_ALIASES = List.of("ВИДЕОКАМЕРА", "КАМЕРА", "IP КАМЕРА", "CAMERA", "IP CAMERA");
    private static final List<String> ACCESS_POINT_ALIASES = List.of("ТОЧКА ДОСТУПА", "ACCESS POINT", "WI-FI", "WIFI");
    private static final List<String> NETWORK_DEVICE_ALIASES = List.of("СЕТЕВОЕ УСТРОЙСТВО", "СЕТЕВАЯ ТОЧКА", "РОЗЕТКА", "СЧИТЫВАТЕЛЬ");
    private static final List<String> TURNSTILE_ALIASES = List.of("ТУРНИКЕТ");

    private final MaterialNormRepository materialNormRepository;
    private final ExpressionEvaluator expressionEvaluator;

    public EndpointCalculator(MaterialNormRepository materialNormRepository, ExpressionEvaluator expressionEvaluator) {
        this.materialNormRepository = materialNormRepository;
        this.expressionEvaluator = expressionEvaluator;
    }

    public Map<Material, Double> calculate(EndpointDevice device) {
        Objects.requireNonNull(device, "Endpoint device is required");
        String normalizedType = normalizeType(device.getType());
        if (normalizedType.isEmpty()) {
            throw new IllegalArgumentException("Endpoint device type is required");
        }

        Map<Material, Double> result = new HashMap<>();

        if (matches(normalizedType, CAMERA_ALIASES)) {
            addMountMaterials(device, CAMERA_MOUNT_POINTS, result);
            addRj45Materials(CAMERA_RJ45_COUNT, result);
        } else if (matches(normalizedType, ACCESS_POINT_ALIASES)) {
            addMountMaterials(device, ACCESS_POINT_MOUNT_POINTS, result);
            addRj45Materials(ACCESS_POINT_RJ45_COUNT, result);
        } else if (matches(normalizedType, NETWORK_DEVICE_ALIASES)) {
            addMountMaterials(device, CAMERA_MOUNT_POINTS, result);
            addRj45Materials(NETWORK_DEVICE_RJ45_COUNT, result);
        } else if (matches(normalizedType, TURNSTILE_ALIASES)) {
            addTurnstileAnchors(result);
        } else {
            throw new IllegalArgumentException("Unsupported endpoint device type: " + device.getType());
        }

        return result;
    }

    private void addMountMaterials(EndpointDevice device, int mountPoints, Map<Material, Double> result) {
        String mountSurface = device.getMountSurface();
        String contextType = ENDPOINT_MOUNT_PREFIX;
        if (StringUtils.hasText(mountSurface)) {
            contextType += "_" + mountSurface.trim().toUpperCase(Locale.ROOT);
        }
        addFromNorm(result, contextType, Map.of("points", mountPoints));
    }

    private void addRj45Materials(int connectorCount, Map<Material, Double> result) {
        addFromNorm(result, RJ45_CONNECTOR, Map.of("count", connectorCount));
    }

    private void addTurnstileAnchors(Map<Material, Double> result) {
        addFromNorm(result, TURNSTILE_ANCHOR, Map.of("count", TURNSTILE_ANCHORS));
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

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type
                .replaceAll("[^A-Za-zА-Яа-я0-9 ]", " ")
                .toUpperCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    private boolean matches(String normalizedType, List<String> aliases) {
        for (String alias : aliases) {
            if (normalizedType.contains(alias) || alias.contains(normalizedType)) {
                return true;
            }
        }
        return false;
    }
}
