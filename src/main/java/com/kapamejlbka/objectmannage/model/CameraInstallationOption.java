package com.kapamejlbka.objectmannage.model;

import java.util.Locale;
import java.util.Optional;

public enum CameraInstallationOption {

    ADAPTER("ADAPTER", "Адаптер"),
    PLASTIC_BOX("PLASTIC_BOX", "Пластиковая коробка");

    private final String code;
    private final String displayName;

    CameraInstallationOption(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<CameraInstallationOption> fromCode(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (CameraInstallationOption option : values()) {
            if (option.code.equals(normalized)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }
}
