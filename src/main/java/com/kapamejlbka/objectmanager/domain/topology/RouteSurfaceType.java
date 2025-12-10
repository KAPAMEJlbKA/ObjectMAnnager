package com.kapamejlbka.objectmanager.domain.topology;

public enum RouteSurfaceType {

    BETON_OR_BRICK("Бетон / кирпич"),
    METAL("Металл"),
    WOOD("Дерево"),
    GYPSUM("Гипсокартон");

    private final String displayNameRu;

    RouteSurfaceType(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getCode() {
        return name();
    }

    public static RouteSurfaceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RouteSurfaceType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
