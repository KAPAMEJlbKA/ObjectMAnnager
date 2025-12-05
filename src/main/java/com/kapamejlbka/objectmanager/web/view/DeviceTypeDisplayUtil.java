package com.kapamejlbka.objectmanager.web.view;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DeviceTypeDisplayUtil {

    public record DeviceTypeOption(String code, String displayName) {}

    private static final Map<String, String> DISPLAY_NAMES = Map.of(
            "CAMERA", "Видеокамера",
            "ACCESS_POINT", "Точка доступа",
            "NETWORK_OUTLET", "Сетевая розетка",
            "SENSOR", "Датчик",
            "READER", "Считыватель",
            "TURNSTILE", "Турникет",
            "OTHER_NETWORK_DEVICE", "Сетевое устройство"
    );

    private DeviceTypeDisplayUtil() {
    }

    public static String displayName(String code) {
        if (code == null) {
            return "—";
        }
        return DISPLAY_NAMES.getOrDefault(code.toUpperCase(Locale.ROOT), code);
    }

    public static List<DeviceTypeOption> availableOptions() {
        return DISPLAY_NAMES.entrySet().stream()
                .map(entry -> new DeviceTypeOption(entry.getKey(), entry.getValue()))
                .toList();
    }
}
