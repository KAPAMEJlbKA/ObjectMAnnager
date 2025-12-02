package com.kapamejlbka.objectmanager.domain.calcengine;

public record CalculationResult(boolean successful, String details) {
    public static CalculationResult success(String details) {
        return new CalculationResult(true, details);
    }

    public static CalculationResult failure(String details) {
        return new CalculationResult(false, details);
    }
}
