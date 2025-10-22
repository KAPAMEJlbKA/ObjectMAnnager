package com.kapamejlbka.objectmannage.model;

public enum CableFunction {
    SIGNAL("Сигнальный"),
    POWER("Питающий"),
    UNKNOWN("Не указан");

    private final String displayName;

    CableFunction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
