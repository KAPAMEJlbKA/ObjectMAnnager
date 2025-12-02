package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.dto.RouteSegmentLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.RouteSegmentLinkUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RouteSegmentLinkService {

    private final RouteSegmentLinkRepository routeSegmentLinkRepository;
    private final InstallationRouteRepository installationRouteRepository;
    private final TopologyLinkRepository topologyLinkRepository;

    public RouteSegmentLinkService(
            RouteSegmentLinkRepository routeSegmentLinkRepository,
            InstallationRouteRepository installationRouteRepository,
            TopologyLinkRepository topologyLinkRepository) {
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
        this.installationRouteRepository = installationRouteRepository;
        this.topologyLinkRepository = topologyLinkRepository;
    }

    @Transactional
    public RouteSegmentLink create(RouteSegmentLinkCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Route segment link data is required");
        }
        RouteSegmentLink routeSegmentLink = new RouteSegmentLink();
        applyDto(routeSegmentLink, dto);
        LocalDateTime now = LocalDateTime.now();
        routeSegmentLink.setCreatedAt(now);
        routeSegmentLink.setUpdatedAt(now);
        return routeSegmentLinkRepository.save(routeSegmentLink);
    }

    @Transactional
    public RouteSegmentLink update(Long id, RouteSegmentLinkUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Route segment link data is required");
        }
        RouteSegmentLink routeSegmentLink = getById(id);
        applyDto(routeSegmentLink, dto);
        routeSegmentLink.setUpdatedAt(LocalDateTime.now());
        return routeSegmentLinkRepository.save(routeSegmentLink);
    }

    public List<RouteSegmentLink> listByRoute(Long routeId) {
        return routeSegmentLinkRepository.findByRouteId(routeId);
    }

    @Transactional
    public void delete(Long id) {
        RouteSegmentLink routeSegmentLink = getById(id);
        routeSegmentLinkRepository.delete(routeSegmentLink);
    }

    private RouteSegmentLink getById(Long id) {
        return routeSegmentLinkRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route segment link not found: " + id));
    }

    private void applyDto(RouteSegmentLink routeSegmentLink, RouteSegmentLinkCreateRequest dto) {
        applyDto(routeSegmentLink, dto.getRouteId(), dto.getTopologyLinkId(), dto.getPortionRatio());
    }

    private void applyDto(RouteSegmentLink routeSegmentLink, RouteSegmentLinkUpdateRequest dto) {
        applyDto(routeSegmentLink, dto.getRouteId(), dto.getTopologyLinkId(), dto.getPortionRatio());
    }

    private void applyDto(RouteSegmentLink routeSegmentLink, Long routeId, Long topologyLinkId, Double portionRatio) {
        if (routeId == null) {
            throw new IllegalArgumentException("Route id is required for route segment link");
        }
        if (topologyLinkId == null) {
            throw new IllegalArgumentException("Topology link id is required for route segment link");
        }
        InstallationRoute route = installationRouteRepository
                .findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Installation route not found: " + routeId));
        TopologyLink topologyLink = topologyLinkRepository
                .findById(topologyLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Topology link not found: " + topologyLinkId));
        if (!route.getCalculation().getId().equals(topologyLink.getCalculation().getId())) {
            throw new IllegalArgumentException("Route and topology link must belong to the same calculation");
        }

        if (portionRatio != null && (portionRatio < 0 || portionRatio > 1)) {
            throw new IllegalArgumentException("Portion ratio must be between 0 and 1 inclusive");
        }

        routeSegmentLink.setRoute(route);
        routeSegmentLink.setTopologyLink(topologyLink);
        routeSegmentLink.setPortionRatio(portionRatio);
    }
}
