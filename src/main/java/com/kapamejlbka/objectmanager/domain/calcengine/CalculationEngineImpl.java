package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CalculationEngineImpl implements CalculationEngine {

    private final SystemCalculationRepository systemCalculationRepository;
    private final EndpointDeviceRepository endpointDeviceRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final TopologyLinkRepository topologyLinkRepository;
    private final InstallationRouteRepository installationRouteRepository;
    private final RouteSegmentLinkRepository routeSegmentLinkRepository;

    public CalculationEngineImpl(
            SystemCalculationRepository systemCalculationRepository,
            EndpointDeviceRepository endpointDeviceRepository,
            NetworkNodeRepository networkNodeRepository,
            TopologyLinkRepository topologyLinkRepository,
            InstallationRouteRepository installationRouteRepository,
            RouteSegmentLinkRepository routeSegmentLinkRepository) {
        this.systemCalculationRepository = systemCalculationRepository;
        this.endpointDeviceRepository = endpointDeviceRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.topologyLinkRepository = topologyLinkRepository;
        this.installationRouteRepository = installationRouteRepository;
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
    }

    @Override
    public CalculationResult calculate(Long calculationId) {
        SystemCalculation calculation = systemCalculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("System calculation not found: " + calculationId));

        List<EndpointDevice> endpointDevices = endpointDeviceRepository.findByCalculationId(calculationId);
        List<NetworkNode> networkNodes = networkNodeRepository.findByCalculationId(calculationId);
        List<TopologyLink> topologyLinks = topologyLinkRepository.findByCalculationId(calculationId);
        List<InstallationRoute> installationRoutes = installationRouteRepository.findByCalculationId(calculationId);
        List<RouteSegmentLink> routeSegmentLinks = installationRoutes.stream()
                .map(route -> routeSegmentLinkRepository.findByRouteId(route.getId()))
                .flatMap(List::stream)
                .toList();

        return new CalculationResult(calculation.getId(), List.of());
    }
}
