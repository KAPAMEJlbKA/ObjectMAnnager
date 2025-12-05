package com.kapamejlbka.objectmanager.domain.topology.dto;

public record TopologyLinkRestUpdateRequest(
        String linkType,
        Double length,
        Integer fiberCores,
        Integer fiberSpliceCount,
        Integer fiberConnectorCount) {}
