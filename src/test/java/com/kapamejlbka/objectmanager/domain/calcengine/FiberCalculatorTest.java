package com.kapamejlbka.objectmanager.domain.calcengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FiberCalculatorTest {

    private MaterialNormRepository materialNormRepository;
    private final com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator expressionEvaluator =
            new com.kapamejlbka.objectmanager.domain.calcengine.dsl.ExpressionEvaluator();
    private FiberCalculator fiberCalculator;
    private final Map<String, MaterialNorm> norms = new HashMap<>();

    @BeforeEach
    void setUp() {
        materialNormRepository = Mockito.mock(MaterialNormRepository.class);
        when(materialNormRepository.findByContextType(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(norms.get(invocation.getArgument(0))));
        fiberCalculator = new FiberCalculator(materialNormRepository, expressionEvaluator);
    }

    @AfterEach
    void tearDown() {
        norms.clear();
    }

    @Test
    void calculatesFiberMaterials() {
        Material fiberCable = material("FIBER_8");
        Material splice = material("FIBER_SPLICE");
        Material connector = material("FIBER_CONNECTOR");
        norms.put("FIBER_8", norm("FIBER_8", fiberCable, "length"));
        norms.put("FIBER_SPLICE", norm("FIBER_SPLICE", splice, "fiberSpliceCount"));
        norms.put("FIBER_CONNECTOR", norm("FIBER_CONNECTOR", connector, "fiberConnectorCount"));

        TopologyLink fiberLink = new TopologyLink();
        fiberLink.setCableLength(120.5);
        fiberLink.setFiberCores(8);
        fiberLink.setFiberSpliceCount(3);
        fiberLink.setFiberConnectorCount(2);

        Map<Material, Double> result = fiberCalculator.calculateForFiberLink(fiberLink);

        assertEquals(120.5, result.get(fiberCable));
        assertEquals(3, result.get(splice));
        assertEquals(2, result.get(connector));
        assertEquals(3, result.size());
    }

    @Test
    void returnsEmptyWhenNormMissing() {
        when(materialNormRepository.findByContextType(anyString())).thenReturn(Optional.empty());

        TopologyLink fiberLink = new TopologyLink();
        fiberLink.setCableLength(10.0);
        fiberLink.setFiberCores(4);

        Map<Material, Double> result = fiberCalculator.calculateForFiberLink(fiberLink);
        assertTrue(result.isEmpty());
    }

    private Material material(String code) {
        Material material = new Material();
        material.setCode(code);
        material.setName(code);
        material.setUnit("unit");
        material.setCategory("category");
        return material;
    }

    private MaterialNorm norm(String contextType, Material material, String formula) {
        MaterialNorm norm = new MaterialNorm();
        norm.setContextType(contextType);
        norm.setMaterial(material);
        norm.setFormula(formula);
        return norm;
    }
}
