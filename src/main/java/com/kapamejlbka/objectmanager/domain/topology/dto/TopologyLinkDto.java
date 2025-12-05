package com.kapamejlbka.objectmanager.domain.topology.dto;

public record TopologyLinkDto(
        Long id,
        Long fromNodeId,
        Long toNodeId,
        Long fromDeviceId,
        Long toDeviceId,
        String linkType,
        Double length,
        Integer fiberCores,
        Integer fiberSpliceCount,
        Integer fiberConnectorCount) {}
