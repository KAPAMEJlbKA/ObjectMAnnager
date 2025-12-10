package com.kapamejlbka.objectmanager.domain.topology;

import java.util.List;

public enum MountSurfaceType {

    WALL("Стена"),
    CEILING("Потолок"),
    POLE("Опора"),
    RACK("Стойка/шкаф"),
    UNKNOWN("Не выбрано");

    private final String displayNameRu;

    MountSurfaceType(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getCode() {
        return name();
    }

    public static List<MountSurfaceType> endpointSurfaces() {
        return List.of(WALL, CEILING, POLE, UNKNOWN);
    }

    public static List<MountSurfaceType> nodeSurfaces() {
        return List.of(WALL, CEILING, POLE, RACK);
    }
}
