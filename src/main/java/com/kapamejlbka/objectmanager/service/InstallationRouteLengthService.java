package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InstallationRouteLengthService {

    private final InstallationRouteRepository installationRouteRepository;
    private final RouteSegmentLinkRepository routeSegmentLinkRepository;

    public InstallationRouteLengthService(
            InstallationRouteRepository installationRouteRepository,
            RouteSegmentLinkRepository routeSegmentLinkRepository) {
        this.installationRouteRepository = installationRouteRepository;
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
    }

    @Transactional
    public void recalculateRouteLength(Long routeId) {
        if (routeId == null) {
            return;
        }
        InstallationRoute route = installationRouteRepository
                .findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
        double maxLength = routeSegmentLinkRepository.findByRouteId(routeId).stream()
                .map(RouteSegmentLink::getTopologyLink)
                .filter(Objects::nonNull)
                .map(TopologyLink::getCableLength)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0d);
        route.setLengthMeters(maxLength);
        installationRouteRepository.save(route);
    }

    @Transactional
    public void recalculateRouteLengths(Collection<Long> routeIds) {
        if (routeIds == null || routeIds.isEmpty()) {
            return;
        }
        routeIds.stream().filter(Objects::nonNull).forEach(this::recalculateRouteLength);
    }

    @Transactional
    public void recalculateForLink(Long topologyLinkId) {
        if (topologyLinkId == null) {
            return;
        }
        Set<Long> routeIds = new HashSet<>();
        routeSegmentLinkRepository.findByTopologyLinkId(topologyLinkId).stream()
                .map(RouteSegmentLink::getRoute)
                .filter(Objects::nonNull)
                .map(InstallationRoute::getId)
                .forEach(routeIds::add);
        recalculateRouteLengths(routeIds);
    }
}
