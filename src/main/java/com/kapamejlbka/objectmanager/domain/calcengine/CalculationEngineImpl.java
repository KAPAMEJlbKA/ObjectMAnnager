package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.calcengine.routes.RouteCalculator;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CalculationEngineImpl implements CalculationEngine {

    private final SystemCalculationRepository systemCalculationRepository;
    private final EndpointDeviceRepository endpointDeviceRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final TopologyLinkRepository topologyLinkRepository;
    private final InstallationRouteRepository installationRouteRepository;
    private final RouteSegmentLinkRepository routeSegmentLinkRepository;
    private final RouteCalculator routeCalculator;
    private final EndpointCalculator endpointCalculator;
    private final NodeCalculator nodeCalculator;
    private final FiberCalculator fiberCalculator;

    public CalculationEngineImpl(
            SystemCalculationRepository systemCalculationRepository,
            EndpointDeviceRepository endpointDeviceRepository,
            NetworkNodeRepository networkNodeRepository,
            TopologyLinkRepository topologyLinkRepository,
            InstallationRouteRepository installationRouteRepository,
            RouteSegmentLinkRepository routeSegmentLinkRepository,
            RouteCalculator routeCalculator,
            EndpointCalculator endpointCalculator,
            NodeCalculator nodeCalculator,
            FiberCalculator fiberCalculator) {
        this.systemCalculationRepository = systemCalculationRepository;
        this.endpointDeviceRepository = endpointDeviceRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.topologyLinkRepository = topologyLinkRepository;
        this.installationRouteRepository = installationRouteRepository;
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
        this.routeCalculator = routeCalculator;
        this.endpointCalculator = endpointCalculator;
        this.nodeCalculator = nodeCalculator;
        this.fiberCalculator = fiberCalculator;
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
        Map<Long, List<RouteSegmentLink>> routeToSegments = installationRoutes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        InstallationRoute::getId,
                        route -> routeSegmentLinkRepository.findByRouteId(route.getId())));

        Map<Material, Double> materialTotals = new HashMap<>();

        installationRoutes.forEach(route -> {
            List<TopologyLink> linksInRoute = routeToSegments.getOrDefault(route.getId(), List.of()).stream()
                    .map(RouteSegmentLink::getTopologyLink)
                    .toList();
            merge(materialTotals, routeCalculator.calculateForRoute(route, linksInRoute));
        });

        endpointDevices.forEach(device -> merge(materialTotals, endpointCalculator.calculate(device)));
        networkNodes.forEach(node -> merge(materialTotals, nodeCalculator.calculate(node)));
        topologyLinks.stream()
                .filter(link -> link.getLinkType() != null && "FIBER".equalsIgnoreCase(link.getLinkType()))
                .forEach(link -> merge(materialTotals, fiberCalculator.calculate(link)));

        List<MaterialItemResult> items = materialTotals.entrySet().stream()
                .map(entry -> new MaterialItemResult(
                        entry.getKey().getCode(),
                        entry.getKey().getName(),
                        entry.getKey().getUnit(),
                        entry.getValue()))
                .toList();

        return new CalculationResult(calculation.getId(), items);
    }

    private void merge(Map<Material, Double> target, Map<Material, Double> addition) {
        addition.forEach((material, quantity) -> target.merge(material, quantity, Double::sum));
    }
}
