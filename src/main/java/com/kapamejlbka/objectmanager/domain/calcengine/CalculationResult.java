package com.kapamejlbka.objectmanager.domain.calcengine;

import java.time.LocalDateTime;
import java.util.List;

public record CalculationResult(Long calculationId, List<MaterialItemResult> items, LocalDateTime executedAt) {}
