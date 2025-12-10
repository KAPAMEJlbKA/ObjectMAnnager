package com.kapamejlbka.objectmanager.domain.calcengine;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.calcengine.routes.RouteCalculator;
import com.kapamejlbka.objectmanager.domain.calcengine.LinkCalculator;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import com.kapamejlbka.objectmanager.service.SettingsService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalculationEngineImpl implements CalculationEngine {

    private static final Logger LOG = LoggerFactory.getLogger(CalculationEngineImpl.class);

    private final SystemCalculationRepository systemCalculationRepository;
    private final EndpointDeviceRepository endpointDeviceRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final TopologyLinkRepository topologyLinkRepository;
    private final InstallationRouteRepository installationRouteRepository;
    private final RouteSegmentLinkRepository routeSegmentLinkRepository;
    private final RouteCalculator routeCalculator;
    private final LinkCalculator linkCalculator;
    private final EndpointCalculator endpointCalculator;
    private final NodeCalculator nodeCalculator;
    private final FiberCalculator fiberCalculator;
    private final SettingsService settingsService;

    public CalculationEngineImpl(
            SystemCalculationRepository systemCalculationRepository,
            EndpointDeviceRepository endpointDeviceRepository,
            NetworkNodeRepository networkNodeRepository,
            TopologyLinkRepository topologyLinkRepository,
            InstallationRouteRepository installationRouteRepository,
            RouteSegmentLinkRepository routeSegmentLinkRepository,
            RouteCalculator routeCalculator,
            LinkCalculator linkCalculator,
            EndpointCalculator endpointCalculator,
            NodeCalculator nodeCalculator,
            FiberCalculator fiberCalculator,
            SettingsService settingsService) {
        this.systemCalculationRepository = systemCalculationRepository;
        this.endpointDeviceRepository = endpointDeviceRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.topologyLinkRepository = topologyLinkRepository;
        this.installationRouteRepository = installationRouteRepository;
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
        this.routeCalculator = routeCalculator;
        this.linkCalculator = linkCalculator;
        this.endpointCalculator = endpointCalculator;
        this.nodeCalculator = nodeCalculator;
        this.fiberCalculator = fiberCalculator;
        this.settingsService = settingsService;
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
        CalculationSettingsDto settings = settingsService.getSettings();
        Map<Long, List<RouteSegmentLink>> routeToSegments = installationRoutes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        InstallationRoute::getId,
                        route -> routeSegmentLinkRepository.findByRouteId(route.getId())));

        Map<Material, Double> materialTotals = new HashMap<>();

        installationRoutes.forEach(route -> {
            try {
                List<TopologyLink> linksInRoute = routeToSegments.getOrDefault(route.getId(), List.of()).stream()
                        .map(RouteSegmentLink::getTopologyLink)
                        .toList();
                if (linksInRoute.isEmpty()) {
                    return;
                }
                merge(materialTotals, routeCalculator.calculateForRoute(route, linksInRoute, settings));
            } catch (Exception ex) {
                LOG.warn("Failed to calculate materials for route {}: {}", route.getName(), ex.getMessage());
            }
        });

        topologyLinks.forEach(link -> {
            try {
                merge(materialTotals, linkCalculator.calculateForLink(link));
            } catch (Exception ex) {
                LOG.warn("Failed to calculate materials for link {}: {}", link.getId(), ex.getMessage());
            }
        });

        endpointDevices.forEach(device -> {
            try {
                merge(materialTotals, endpointCalculator.calculateForDevice(device));
            } catch (Exception ex) {
                LOG.warn("Failed to calculate materials for endpoint {}: {}", device.getName(), ex.getMessage());
            }
        });

        Map<Long, Long> nodeIncomingCounts = topologyLinks.stream()
                .flatMap(link -> Stream.of(link.getFromNode(), link.getToNode()))
                .filter(node -> node != null && node.getId() != null)
                .collect(Collectors.groupingBy(NetworkNode::getId, Collectors.counting()));

        networkNodes.forEach(node -> {
            try {
                int incoming = nodeIncomingCounts.getOrDefault(node.getId(), 0L).intValue();
                if (node.getIncomingLinesCount() != null) {
                    incoming = Math.max(incoming, node.getIncomingLinesCount());
                }
                merge(materialTotals, nodeCalculator.calculateForNode(node, incoming, settings));
            } catch (Exception ex) {
                LOG.warn("Failed to calculate materials for node {}: {}", node.getName(), ex.getMessage());
            }
        });

        topologyLinks.stream()
                .filter(link -> link.getLinkType() != null && "FIBER".equalsIgnoreCase(link.getLinkType()))
                .forEach(link -> {
                    try {
                        merge(materialTotals, fiberCalculator.calculateForFiberLink(link));
                    } catch (Exception ex) {
                        LOG.warn("Failed to calculate materials for fiber link {}: {}", link.getId(), ex.getMessage());
                    }
                });

        List<MaterialItemResult> items = materialTotals.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Material, Double> e) -> e.getKey().getCategory())
                        .thenComparing(e -> e.getKey().getName()))
                .map(entry -> new MaterialItemResult(
                        entry.getKey().getCode(),
                        entry.getKey().getName(),
                        entry.getKey().getCategory(),
                        entry.getKey().getUnit(),
                        entry.getValue()))
                .toList();

        return new CalculationResult(calculation.getId(), items, LocalDateTime.now());
    }

    private void merge(Map<Material, Double> target, Map<Material, Double> addition) {
        if (addition == null) {
            return;
        }
        addition.forEach((material, quantity) -> target.merge(material, quantity, Double::sum));
    }
}
