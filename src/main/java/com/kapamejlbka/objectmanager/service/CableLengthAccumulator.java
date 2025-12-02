package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.device.CableFunction;

class CableLengthAccumulator {

    private final String name;
    private double totalLength;
    private boolean classificationMissing;
    private final CableFunction function;

    CableLengthAccumulator(String name, double totalLength, boolean classificationMissing, CableFunction function) {
        this.name = name;
        this.totalLength = totalLength;
        this.classificationMissing = classificationMissing;
        this.function = function;
    }

    void add(double value) {
        this.totalLength += value;
    }

    void setClassificationMissing(boolean classificationMissing) {
        this.classificationMissing = classificationMissing;
    }

    String name() {
        return name;
    }

    double totalLength() {
        return totalLength;
    }

    boolean classificationMissing() {
        return classificationMissing;
    }

    CableFunction function() {
        return function;
    }
}
