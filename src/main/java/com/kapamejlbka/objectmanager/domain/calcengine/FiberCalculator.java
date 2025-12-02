package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class FiberCalculator {

    private static final String FIBER_CABLE_TEMPLATE = "FIBER_%s_CORES";
    private static final String FIBER_SPLICE_SLEEVE = "FIBER_SPLICE_SLEEVE";
    private static final String FIBER_SPLICE_CASSETTE = "FIBER_SPLICE_CASSETTE";
    private static final String FIBER_CONNECTOR = "FIBER_CONNECTOR";

    private final MaterialRepository materialRepository;

    public FiberCalculator(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public Map<Material, Double> calculate(TopologyLink fiberLink) {
        Objects.requireNonNull(fiberLink, "Fiber link is required");

        Double cableLength = Objects.requireNonNull(fiberLink.getCableLength(), "Fiber cable length is required");
        Integer fiberCores = Objects.requireNonNull(fiberLink.getFiberCores(), "Fiber cores are required");
        Integer fiberSpliceCount = defaultZero(fiberLink.getFiberSpliceCount());
        Integer fiberConnectorCount = defaultZero(fiberLink.getFiberConnectorCount());

        Map<Material, Double> result = new HashMap<>();

        Material fiberCable = findByCode(FIBER_CABLE_TEMPLATE.formatted(fiberCores));
        result.put(fiberCable, cableLength);

        if (fiberSpliceCount > 0) {
            Material spliceSleeve = findByCode(FIBER_SPLICE_SLEEVE);
            Material spliceCassette = findByCode(FIBER_SPLICE_CASSETTE);
            result.put(spliceSleeve, fiberSpliceCount.doubleValue());
            result.put(spliceCassette, fiberSpliceCount.doubleValue());
        }

        if (fiberConnectorCount > 0) {
            Material connector = findByCode(FIBER_CONNECTOR);
            result.put(connector, fiberConnectorCount.doubleValue());
        }

        return result;
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Material findByCode(String code) {
        return materialRepository
                .findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Material not found for code: " + code));
    }
}
