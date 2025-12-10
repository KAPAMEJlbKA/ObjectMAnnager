package com.kapamejlbka.objectmanager.domain.device;

public enum EndpointDeviceType {

    CAMERA("Видеокамера"),
    ACCESS_POINT("Точка доступа"),
    NETWORK_OUTLET("Сетевая розетка"),
    READER("Считыватель"),
    TURNSTILE("Турникет"),
    OTHER_NETWORK_DEVICE("Сетевое устройство");

    private final String displayNameRu;

    EndpointDeviceType(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getCode() {
        return name();
    }

    public static EndpointDeviceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EndpointDeviceType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
