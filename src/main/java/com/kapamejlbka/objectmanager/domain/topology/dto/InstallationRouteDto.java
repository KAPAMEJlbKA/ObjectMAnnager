package com.kapamejlbka.objectmanager.domain.topology.dto;

public record InstallationRouteDto(
        Long id,
        String name,
        String routeType,
        String surfaceType,
        Double length,
        String orientation,
        String fixingMethod) {}
