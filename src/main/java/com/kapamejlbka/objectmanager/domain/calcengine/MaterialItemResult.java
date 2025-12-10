package com.kapamejlbka.objectmanager.domain.calcengine;

public record MaterialItemResult(
        String materialCode,
        String materialName,
        String category,
        String unit,
        double quantity) {}
