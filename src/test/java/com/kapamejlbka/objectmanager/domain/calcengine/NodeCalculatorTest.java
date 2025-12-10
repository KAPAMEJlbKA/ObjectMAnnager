package com.kapamejlbka.objectmanager.domain.calcengine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NodeCalculatorTest {

    @Mock
    private MaterialNormRepository materialNormRepository;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @InjectMocks
    private NodeCalculator nodeCalculator;

    @Test
    void calculateSkipsCabinetWhenSizeMissing() {
        when(materialNormRepository.findAllByContextType(anyString())).thenReturn(List.of());

        NetworkNode node = new NetworkNode();
        node.setBaseCircuitBreakers(1);
        node.setExtraCircuitBreakers(0);
        node.setBaseSockets(1);
        node.setExtraSockets(0);

        Map<Material, Double> result = nodeCalculator.calculateForNode(node, 0, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateReturnsValuesWhenNormsPresent() {
        Material cabinet = new Material();
        cabinet.setCode("CABINET");
        cabinet.setName("Cabinet");
        cabinet.setUnit("pc");
        cabinet.setCategory("nodes");

        MaterialNorm norm = new MaterialNorm();
        norm.setContextType("NODE_CABINET_350");
        norm.setMaterial(cabinet);
        norm.setFormula("1");

        when(materialNormRepository.findAllByContextType("NODE_CABINET_350")).thenReturn(List.of(norm));
        when(expressionEvaluator.evaluate(anyString(), anyMap())).thenReturn(1.0);

        NetworkNode node = new NetworkNode();
        node.setCabinetSize(350);
        node.setBaseCircuitBreakers(0);
        node.setExtraCircuitBreakers(0);
        node.setBaseSockets(0);
        node.setExtraSockets(0);

        Map<Material, Double> result = nodeCalculator.calculateForNode(node, 0, null);

        assertNotNull(result);
        assertTrue(result.containsKey(cabinet));
    }
}
