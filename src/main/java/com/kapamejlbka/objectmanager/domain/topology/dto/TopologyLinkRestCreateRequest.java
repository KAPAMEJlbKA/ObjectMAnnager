package com.kapamejlbka.objectmanager.domain.topology.dto;

public record TopologyLinkRestCreateRequest(
        Long fromNodeId,
        Long toNodeId,
        Long fromDeviceId,
        Long toDeviceId,
        String linkType) {}
