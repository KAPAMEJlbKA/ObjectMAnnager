package com.kapamejlbka.objectmanager.legacy.objectmannage.domain;

public enum DeviceType {
    CAMERA("Камера"),
    NVR("NVR"),
    SWITCH("Коммутатор"),
    ACCESS_POINT("Точка доступа"),
    OTHER("Другое");

    private final String displayName;

    DeviceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
