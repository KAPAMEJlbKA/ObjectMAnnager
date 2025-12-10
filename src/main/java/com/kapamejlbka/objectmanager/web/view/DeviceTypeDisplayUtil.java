package com.kapamejlbka.objectmanager.web.view;

import com.kapamejlbka.objectmanager.domain.device.EndpointDeviceType;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DeviceTypeDisplayUtil {

    public record DeviceTypeOption(String code, String displayName) {}

    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry(EndpointDeviceType.CAMERA.name(), EndpointDeviceType.CAMERA.getDisplayNameRu()),
            Map.entry(EndpointDeviceType.ACCESS_POINT.name(), EndpointDeviceType.ACCESS_POINT.getDisplayNameRu()),
            Map.entry(EndpointDeviceType.NETWORK_OUTLET.name(), EndpointDeviceType.NETWORK_OUTLET.getDisplayNameRu()),
            Map.entry(EndpointDeviceType.READER.name(), EndpointDeviceType.READER.getDisplayNameRu()),
            Map.entry(EndpointDeviceType.TURNSTILE.name(), EndpointDeviceType.TURNSTILE.getDisplayNameRu()),
            Map.entry(
                    EndpointDeviceType.OTHER_NETWORK_DEVICE.name(),
                    EndpointDeviceType.OTHER_NETWORK_DEVICE.getDisplayNameRu())
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
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> new DeviceTypeOption(entry.getKey(), entry.getValue()))
                .toList();
    }
}
