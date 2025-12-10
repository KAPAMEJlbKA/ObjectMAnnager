package com.kapamejlbka.objectmanager.domain.topology.dto;

public record RouteLinkDto(
        Long id,
        Long fromNodeId,
        Long toNodeId,
        Long fromDeviceId,
        Long toDeviceId,
        Long routeId,
        Double length,
        String linkType) {}
