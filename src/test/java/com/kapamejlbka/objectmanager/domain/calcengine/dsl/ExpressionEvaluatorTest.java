package com.kapamejlbka.objectmanager.domain.calcengine.dsl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionEvaluatorTest {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Test
    void evaluatesArithmeticExpressionsWithContextVariables() {
        Map<String, Object> context = Map.of(
                "length", 12,
                "deviceCount", 3,
                "extraSockets", 4,
                "fiberCores", 10
        );

        double result = evaluator.evaluate("length * deviceCount + extraSockets - fiberCores / 2", context);

        assertEquals(12 * 3 + 4 - 10 / 2.0, result);
    }

    @Test
    void supportsProvidedHelperFunctions() {
        double result = evaluator.evaluate("ceil(10.2) + floor(3.9) + max(1, 5) - min(3, 8)", Map.of());

        assertEquals(16.0, result);
    }

    @Test
    void throwsIllegalArgumentForInvalidExpressions() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("length +", Map.of("length", 5)));
    }
}
