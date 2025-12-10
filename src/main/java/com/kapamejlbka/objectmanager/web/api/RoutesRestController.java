package com.kapamejlbka.objectmanager.web.api;

import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.dto.MaterialOptionDto;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteDto;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.LinkAssignmentRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.RouteLinkDto;
import com.kapamejlbka.objectmanager.domain.topology.dto.RouteSegmentLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.RoutesResponse;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.RouteSegmentLinkRepository;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import com.kapamejlbka.objectmanager.service.InstallationRouteLengthService;
import com.kapamejlbka.objectmanager.service.InstallationRouteService;
import com.kapamejlbka.objectmanager.service.RouteSegmentLinkService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/calculations/{calcId}/routes")
public class RoutesRestController {

    private static final Set<String> ROUTE_MATERIAL_CATEGORIES = Set.of(
            "ROUTE_CORRUGATED_PIPE", "ROUTE_CABLE_CHANNEL", "ROUTE_WIRE_ROPE", "ROUTE_BARE_CABLE");

    private final SystemCalculationRepository calculationRepository;
    private final InstallationRouteRepository installationRouteRepository;
    private final TopologyLinkRepository topologyLinkRepository;
    private final RouteSegmentLinkRepository routeSegmentLinkRepository;
    private final MaterialRepository materialRepository;
    private final InstallationRouteService installationRouteService;
    private final RouteSegmentLinkService routeSegmentLinkService;
    private final InstallationRouteLengthService installationRouteLengthService;

    public RoutesRestController(
            SystemCalculationRepository calculationRepository,
            InstallationRouteRepository installationRouteRepository,
            TopologyLinkRepository topologyLinkRepository,
            RouteSegmentLinkRepository routeSegmentLinkRepository,
            MaterialRepository materialRepository,
            InstallationRouteService installationRouteService,
            RouteSegmentLinkService routeSegmentLinkService,
            InstallationRouteLengthService installationRouteLengthService) {
        this.calculationRepository = calculationRepository;
        this.installationRouteRepository = installationRouteRepository;
        this.topologyLinkRepository = topologyLinkRepository;
        this.routeSegmentLinkRepository = routeSegmentLinkRepository;
        this.materialRepository = materialRepository;
        this.installationRouteService = installationRouteService;
        this.routeSegmentLinkService = routeSegmentLinkService;
        this.installationRouteLengthService = installationRouteLengthService;
    }

    @GetMapping
    public RoutesResponse list(@PathVariable("calcId") Long calculationId) {
        ensureCalculationExists(calculationId);
        List<InstallationRoute> routes = installationRouteRepository.findByCalculationId(calculationId);
        List<RouteSegmentLink> assignments = routeSegmentLinkRepository.findByRouteCalculation_Id(calculationId);
        List<TopologyLink> links = topologyLinkRepository.findByCalculationId(calculationId);
        Set<Long> selectedMaterialIds = routes.stream()
                .map(InstallationRoute::getMainMaterial)
                .filter(Objects::nonNull)
                .map(Material::getId)
                .collect(Collectors.toSet());
        List<MaterialOptionDto> materials = materialRepository.findAll().stream()
                .filter(material -> isRouteMaterial(material) || selectedMaterialIds.contains(material.getId()))
                .sorted(Comparator.comparing(Material::getCategory).thenComparing(Material::getName))
                .map(material -> new MaterialOptionDto(material.getId(), material.getName(), material.getCategory()))
                .toList();

        Map<Long, Long> linkToRoute = assignments.stream()
                .filter(rl -> rl.getTopologyLink() != null && rl.getRoute() != null)
                .collect(Collectors.toMap(
                        rl -> rl.getTopologyLink().getId(),
                        rl -> rl.getRoute().getId(),
                        (existing, replacement) -> existing));

        List<InstallationRouteDto> routeDtos = routes.stream()
                .sorted(Comparator.comparing(InstallationRoute::getName, String.CASE_INSENSITIVE_ORDER))
                .map(route -> new InstallationRouteDto(
                        route.getId(),
                        route.getName(),
                        route.getRouteType(),
                        route.getMountSurface(),
                        Optional.ofNullable(route.getLengthMeters()).orElse(0d),
                        route.getOrientation(),
                        route.getFixingMethod(),
                        Optional.ofNullable(route.getMainMaterial()).map(Material::getId).orElse(null),
                        Optional.ofNullable(route.getMainMaterial()).map(Material::getName).orElse(null),
                        Optional.ofNullable(route.getMainMaterial()).map(Material::getCategory).orElse(null)))
                .toList();

        List<RouteLinkDto> linkDtos = links.stream()
                .map(link -> new RouteLinkDto(
                        link.getId(),
                        link.getFromNode() != null ? link.getFromNode().getId() : null,
                        link.getToNode() != null ? link.getToNode().getId() : null,
                        link.getFromDevice() != null ? link.getFromDevice().getId() : null,
                        link.getToDevice() != null ? link.getToDevice().getId() : null,
                        linkToRoute.get(link.getId()),
                        link.getCableLength(),
                        link.getLinkType()))
                .toList();

        return new RoutesResponse(routeDtos, linkDtos, materials);
    }

    @PostMapping
    public InstallationRouteDto create(
            @PathVariable("calcId") Long calculationId,
            @RequestBody InstallationRouteCreateRequest payload) {
        ensureCalculationExists(calculationId);
        try {
            InstallationRoute created = installationRouteService.create(calculationId, payload);
            return toDto(created, 0d);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/{routeId}")
    public InstallationRouteDto update(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("routeId") Long routeId,
            @RequestBody InstallationRouteUpdateRequest payload) {
        InstallationRoute existing = findRoute(calculationId, routeId);
        InstallationRouteUpdateRequest dto = new InstallationRouteUpdateRequest();
        dto.setName(Optional.ofNullable(payload.getName()).orElse(existing.getName()));
        dto.setRouteType(Optional.ofNullable(payload.getRouteType()).orElse(existing.getRouteType()));
        dto.setMountSurface(Optional.ofNullable(payload.getMountSurface()).orElse(existing.getMountSurface()));
        dto.setLengthMeters(existing.getLengthMeters());
        dto.setOrientation(Optional.ofNullable(payload.getOrientation()).orElse(existing.getOrientation()));
        dto.setFixingMethod(Optional.ofNullable(payload.getFixingMethod()).orElse(existing.getFixingMethod()));
        dto.setMainMaterialId(Optional.ofNullable(payload.getMainMaterialId())
                .orElse(Optional.ofNullable(existing.getMainMaterial()).map(Material::getId).orElse(null)));
        try {
            InstallationRoute updated = installationRouteService.update(routeId, dto);
            return toDto(updated, existing.getLengthMeters());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/{routeId}")
    public void delete(
            @PathVariable("calcId") Long calculationId, @PathVariable("routeId") Long routeId) {
        findRoute(calculationId, routeId);
        routeSegmentLinkRepository.deleteByRouteId(routeId);
        installationRouteService.delete(routeId);
    }

    @PostMapping("/{routeId}/assign-link")
    public RouteLinkDto assignLink(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("routeId") Long routeId,
            @RequestBody LinkAssignmentRequest payload) {
        InstallationRoute route = findRoute(calculationId, routeId);
        if (payload == null || payload.linkId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link id is required");
        }
        TopologyLink link = findLink(calculationId, payload.linkId());
        Set<Long> affectedRouteIds = new HashSet<>();

        List<RouteSegmentLink> existingAssignments = routeSegmentLinkRepository.findByTopologyLinkId(link.getId());
        existingAssignments.stream()
                .filter(assignment -> !Objects.equals(assignment.getRoute().getId(), route.getId()))
                .forEach(assignment -> {
                    affectedRouteIds.add(assignment.getRoute().getId());
                    routeSegmentLinkRepository.delete(assignment);
                });
        boolean alreadyAssigned = existingAssignments.stream()
                .anyMatch(assignment -> Objects.equals(assignment.getRoute().getId(), route.getId()));
        if (!alreadyAssigned) {
            RouteSegmentLinkCreateRequest dto = new RouteSegmentLinkCreateRequest();
            dto.setRouteId(route.getId());
            dto.setTopologyLinkId(link.getId());
            routeSegmentLinkService.create(dto);
        }

        affectedRouteIds.add(route.getId());
        installationRouteLengthService.recalculateRouteLengths(affectedRouteIds);

        return new RouteLinkDto(
                link.getId(),
                link.getFromNode() != null ? link.getFromNode().getId() : null,
                link.getToNode() != null ? link.getToNode().getId() : null,
                link.getFromDevice() != null ? link.getFromDevice().getId() : null,
                link.getToDevice() != null ? link.getToDevice().getId() : null,
                route.getId(),
                link.getCableLength(),
                link.getLinkType());
    }

    @PostMapping("/{routeId}/unassign-link")
    public void unassignLink(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("routeId") Long routeId,
            @RequestBody LinkAssignmentRequest payload) {
        findRoute(calculationId, routeId);
        if (payload == null || payload.linkId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link id is required");
        }
        TopologyLink link = findLink(calculationId, payload.linkId());
        routeSegmentLinkRepository.findByTopologyLinkId(link.getId()).stream()
                .filter(assignment -> Objects.equals(assignment.getRoute().getId(), routeId))
                .forEach(routeSegmentLinkRepository::delete);
        installationRouteLengthService.recalculateRouteLength(routeId);
    }

    private InstallationRoute findRoute(Long calculationId, Long routeId) {
        InstallationRoute route = installationRouteRepository
                .findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        if (!route.getCalculation().getId().equals(calculationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found");
        }
        return route;
    }

    private TopologyLink findLink(Long calculationId, Long linkId) {
        TopologyLink link = topologyLinkRepository
                .findById(linkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));
        if (!link.getCalculation().getId().equals(calculationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found");
        }
        return link;
    }

    private void ensureCalculationExists(Long calculationId) {
        calculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calculation not found"));
    }

    private InstallationRouteDto toDto(InstallationRoute route, Double calculatedLength) {
        Double length = calculatedLength != null && calculatedLength > 0
                ? calculatedLength
                : Optional.ofNullable(route.getLengthMeters()).orElse(0d);
        return new InstallationRouteDto(
                route.getId(),
                route.getName(),
                route.getRouteType(),
                route.getMountSurface(),
                length,
                route.getOrientation(),
                route.getFixingMethod(),
                Optional.ofNullable(route.getMainMaterial()).map(Material::getId).orElse(null),
                Optional.ofNullable(route.getMainMaterial()).map(Material::getName).orElse(null),
                Optional.ofNullable(route.getMainMaterial()).map(Material::getCategory).orElse(null));
    }

    private boolean isRouteMaterial(Material material) {
        if (material == null || material.getCategory() == null) {
            return false;
        }
        return ROUTE_MATERIAL_CATEGORIES.contains(material.getCategory());
    }
}
