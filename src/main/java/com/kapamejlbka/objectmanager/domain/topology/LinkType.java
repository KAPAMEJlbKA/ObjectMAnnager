package com.kapamejlbka.objectmanager.domain.topology;

public enum LinkType {

    UTP("Слаботочный кабель (UTP)"),
    FIBER("Оптоволокно"),
    POWER("Силовой кабель"),
    WIFI("Беспроводное соединение");

    private final String displayNameRu;

    LinkType(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getCode() {
        return name();
    }

    public static LinkType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LinkType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
