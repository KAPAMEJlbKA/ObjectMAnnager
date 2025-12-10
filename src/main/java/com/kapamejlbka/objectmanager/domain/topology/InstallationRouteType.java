package com.kapamejlbka.objectmanager.domain.topology;

public enum InstallationRouteType {

    CORRUGATED_PIPE("Гофрированная труба"),
    CABLE_CHANNEL("Кабель-канал"),
    WIRE_ROPE("Трос"),
    BARE_CABLE("Открытая прокладка"),
    TRAY_STRUCTURE("По лоткам/конструкциям");

    private final String displayNameRu;

    InstallationRouteType(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getCode() {
        return name();
    }

    public static InstallationRouteType fromCode(String code) {
        if (code == null) {
            return null;
        }
        if ("TRAY_OR_STRUCTURE".equalsIgnoreCase(code.trim())) {
            return TRAY_STRUCTURE;
        }
        for (InstallationRouteType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
