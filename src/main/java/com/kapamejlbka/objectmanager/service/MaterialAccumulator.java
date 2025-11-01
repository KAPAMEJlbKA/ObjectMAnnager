package com.kapamejlbka.objectmanager.service;

class MaterialAccumulator {

    private final String name;
    private final String unit;
    private double quantity;

    MaterialAccumulator(String name, String unit, double quantity) {
        this.name = trim(name);
        this.unit = trim(unit);
        this.quantity = quantity;
    }

    void add(double value) {
        this.quantity += value;
    }

    String name() {
        return name;
    }

    String unit() {
        return unit;
    }

    double quantity() {
        return quantity;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
