package com.kapamejlbka.objectmanager.domain.device;

import java.util.Locale;
import java.util.Optional;

public enum SurfaceType {

    UNKNOWN("UNKNOWN", "Не выбрано", "Крепёж (уточнить)"),
    WALL("WALL", "По стене", "Дюбель-гвоздь"),
    WOOD("WOOD", "По дереву", "Саморезы по дереву"),
    METAL("METAL", "По металлу", "Саморезы со сверлом"),
    POLE("POLE", "По опоре", "Перфолента или дюбель-гвоздь"),
    SUSPENDED_CEILING("SUSPENDED_CEILING", "По навесному потолку", "Нейлоновые стяжки"),
    EXISTING_STRUCTURES("EXISTING_STRUCTURES", "По готовым конструкциям", "Нейлоновые стяжки"),
    CABLE_TRAY("CABLE_TRAY", "По тросам", "Нейлоновые стяжки");

    private final String code;
    private final String displayName;
    private final String fastenerName;

    SurfaceType(String code, String displayName, String fastenerName) {
        this.code = code;
        this.displayName = displayName;
        this.fastenerName = fastenerName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFastenerName() {
        return fastenerName;
    }

    public static SurfaceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (SurfaceType type : values()) {
            if (type.code.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static SurfaceType fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        String normalized = displayName.trim().toLowerCase(Locale.ROOT);
        for (SurfaceType type : values()) {
            if (type.displayName.toLowerCase(Locale.ROOT).equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static Optional<SurfaceType> resolve(String candidate) {
        if (candidate == null) {
            return Optional.empty();
        }
        SurfaceType byCode = fromCode(candidate);
        if (byCode != null) {
            return Optional.of(byCode);
        }
        SurfaceType byDisplay = fromDisplayName(candidate);
        if (byDisplay != null) {
            return Optional.of(byDisplay);
        }
        return Optional.empty();
    }
}
