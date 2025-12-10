package com.kapamejlbka.objectmanager.domain.topology.dto;

import com.kapamejlbka.objectmanager.domain.material.dto.MaterialOptionDto;
import java.util.List;

public record RoutesResponse(
        List<InstallationRouteDto> routes, List<RouteLinkDto> links, List<MaterialOptionDto> materials) {}
