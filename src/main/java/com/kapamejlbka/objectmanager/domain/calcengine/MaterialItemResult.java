package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.material.MaterialCategory;

public record MaterialItemResult(
        String materialCode,
        String materialName,
        MaterialCategory category,
        String unit,
        double quantity) {}
