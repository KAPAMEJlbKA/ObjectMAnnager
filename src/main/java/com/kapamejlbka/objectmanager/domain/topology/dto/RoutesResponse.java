package com.kapamejlbka.objectmanager.domain.topology.dto;

import java.util.List;

public record RoutesResponse(List<InstallationRouteDto> routes, List<RouteLinkDto> links) {}
