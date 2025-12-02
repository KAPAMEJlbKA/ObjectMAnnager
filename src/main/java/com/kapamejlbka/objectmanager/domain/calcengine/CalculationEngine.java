package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataV2;

public interface CalculationEngine {
    CalculationResult calculate(PrimaryDataV2 primaryDataV2);
}
