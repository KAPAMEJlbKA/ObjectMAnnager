package com.kapamejlbka.objectmanager.domain.calcengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FiberCalculatorTest {

    @Mock
    private MaterialRepository materialRepository;

    private FiberCalculator fiberCalculator;

    @BeforeEach
    void setUp() {
        fiberCalculator = new FiberCalculator(materialRepository);
    }

    @Test
    void calculatesFiberMaterials() {
        Material fiberCable = material("FIBER_8_CORES");
        Material spliceSleeve = material("FIBER_SPLICE_SLEEVE");
        Material spliceCassette = material("FIBER_SPLICE_CASSETTE");
        Material connector = material("FIBER_CONNECTOR");

        when(materialRepository.findByCode("FIBER_8_CORES")).thenReturn(Optional.of(fiberCable));
        when(materialRepository.findByCode("FIBER_SPLICE_SLEEVE")).thenReturn(Optional.of(spliceSleeve));
        when(materialRepository.findByCode("FIBER_SPLICE_CASSETTE")).thenReturn(Optional.of(spliceCassette));
        when(materialRepository.findByCode("FIBER_CONNECTOR")).thenReturn(Optional.of(connector));

        TopologyLink fiberLink = new TopologyLink();
        fiberLink.setCableLength(120.5);
        fiberLink.setFiberCores(8);
        fiberLink.setFiberSpliceCount(3);
        fiberLink.setFiberConnectorCount(2);

        Map<Material, Double> result = fiberCalculator.calculate(fiberLink);

        assertEquals(120.5, result.get(fiberCable));
        assertEquals(3, result.get(spliceSleeve));
        assertEquals(3, result.get(spliceCassette));
        assertEquals(2, result.get(connector));
        assertEquals(4, result.size());
    }

    @Test
    void throwsWhenMaterialNotFound() {
        when(materialRepository.findByCode(anyString())).thenReturn(Optional.empty());

        TopologyLink fiberLink = new TopologyLink();
        fiberLink.setCableLength(10.0);
        fiberLink.setFiberCores(4);

        assertThrows(IllegalArgumentException.class, () -> fiberCalculator.calculate(fiberLink));
    }

    private Material material(String code) {
        Material material = new Material();
        material.setCode(code);
        material.setName(code);
        material.setUnit("unit");
        material.setCategory("category");
        return material;
    }
}
