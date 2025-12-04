package com.kapamejlbka.objectmanager.domain.calcengine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeCalculatorTest {

    @Mock
    private MaterialNormRepository materialNormRepository;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @InjectMocks
    private NodeCalculator nodeCalculator;

    @Test
    void calculateSkipsCabinetWhenSizeMissing() {
        when(materialNormRepository.findByContextType(anyString())).thenReturn(Optional.empty());

        NetworkNode node = new NetworkNode();
        node.setBaseCircuitBreakers(1);
        node.setExtraCircuitBreakers(0);
        node.setBaseSockets(1);
        node.setExtraSockets(0);

        Map<Material, Double> result = nodeCalculator.calculate(node);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
