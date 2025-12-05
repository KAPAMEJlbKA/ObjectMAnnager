package com.kapamejlbka.objectmanager.domain.topology.dto;

import java.util.List;

public record TopologyDto(List<TopologyNodeDto> nodes, List<TopologyDeviceDto> devices, List<TopologyLinkDto> links) {}
