package com.kapamejlbka.objectmanager.web.api;

import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.dto.PositionUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyDeviceDto;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyDto;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkDto;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkRestCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkRestUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyNodeDto;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import com.kapamejlbka.objectmanager.service.TopologyLinkService;
import java.util.List;
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
@RequestMapping("/api/calculations/{calcId}/topology")
public class TopologyRestController {

    private final SystemCalculationRepository calculationRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final EndpointDeviceRepository endpointDeviceRepository;
    private final TopologyLinkRepository topologyLinkRepository;
    private final TopologyLinkService topologyLinkService;

    public TopologyRestController(
            SystemCalculationRepository calculationRepository,
            NetworkNodeRepository networkNodeRepository,
            EndpointDeviceRepository endpointDeviceRepository,
            TopologyLinkRepository topologyLinkRepository,
            TopologyLinkService topologyLinkService) {
        this.calculationRepository = calculationRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.endpointDeviceRepository = endpointDeviceRepository;
        this.topologyLinkRepository = topologyLinkRepository;
        this.topologyLinkService = topologyLinkService;
    }

    @GetMapping
    public TopologyDto getTopology(@PathVariable("calcId") Long calculationId) {
        ensureCalculationExists(calculationId);
        List<TopologyNodeDto> nodes = networkNodeRepository.findByCalculationId(calculationId).stream()
                .map(this::toDto)
                .toList();
        List<TopologyDeviceDto> devices = endpointDeviceRepository.findByCalculationId(calculationId).stream()
                .map(this::toDto)
                .toList();
        List<TopologyLinkDto> links = topologyLinkRepository.findByCalculationId(calculationId).stream()
                .map(this::toDto)
                .toList();
        return new TopologyDto(nodes, devices, links);
    }

    @PostMapping("/nodes/{nodeId}/position")
    public void updateNodePosition(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("nodeId") Long nodeId,
            @RequestBody PositionUpdateRequest body) {
        NetworkNode node = findNode(calculationId, nodeId);
        node.setPosX(body.x());
        node.setPosY(body.y());
        networkNodeRepository.save(node);
    }

    @PostMapping("/devices/{deviceId}/position")
    public void updateDevicePosition(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("deviceId") Long deviceId,
            @RequestBody PositionUpdateRequest body) {
        EndpointDevice device = findDevice(calculationId, deviceId);
        device.setPosX(body.x());
        device.setPosY(body.y());
        endpointDeviceRepository.save(device);
    }

    @PostMapping("/links")
    public TopologyLinkDto createLink(
            @PathVariable("calcId") Long calculationId,
            @RequestBody TopologyLinkRestCreateRequest payload) {
        ensureCalculationExists(calculationId);
        try {
            validateEndpointOwnership(calculationId, payload.fromNodeId(), payload.fromDeviceId());
            validateEndpointOwnership(calculationId, payload.toNodeId(), payload.toDeviceId());
            TopologyLinkCreateRequest dto = new TopologyLinkCreateRequest();
            dto.setFromNodeId(payload.fromNodeId());
            dto.setToNodeId(payload.toNodeId());
            dto.setFromDeviceId(payload.fromDeviceId());
            dto.setToDeviceId(payload.toDeviceId());
            dto.setLinkType(payload.linkType());
            TopologyLink created = topologyLinkService.create(calculationId, dto);
            return toDto(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/links/{linkId}")
    public TopologyLinkDto updateLink(
            @PathVariable("calcId") Long calculationId,
            @PathVariable("linkId") Long linkId,
            @RequestBody TopologyLinkRestUpdateRequest payload) {
        TopologyLink existing = findLink(calculationId, linkId);
        try {
            TopologyLinkUpdateRequest dto = new TopologyLinkUpdateRequest();
            dto.setFromNodeId(existing.getFromNode() != null ? existing.getFromNode().getId() : null);
            dto.setToNodeId(existing.getToNode() != null ? existing.getToNode().getId() : null);
            dto.setFromDeviceId(existing.getFromDevice() != null ? existing.getFromDevice().getId() : null);
            dto.setToDeviceId(existing.getToDevice() != null ? existing.getToDevice().getId() : null);
            dto.setLinkType(payload.linkType() != null ? payload.linkType() : existing.getLinkType());
            dto.setCableLength(payload.length() != null ? payload.length() : existing.getCableLength());
            dto.setWireless(existing.getWireless());
            dto.setFiberCores(payload.fiberCores() != null ? payload.fiberCores() : existing.getFiberCores());
            dto.setFiberSpliceCount(
                    payload.fiberSpliceCount() != null ? payload.fiberSpliceCount() : existing.getFiberSpliceCount());
            dto.setFiberConnectorCount(
                    payload.fiberConnectorCount() != null
                            ? payload.fiberConnectorCount()
                            : existing.getFiberConnectorCount());
            dto.setPowerSourceDescription(existing.getPowerSourceDescription());
            TopologyLink updated = topologyLinkService.update(linkId, dto);
            return toDto(updated);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/links/{linkId}")
    public void deleteLink(@PathVariable("calcId") Long calculationId, @PathVariable("linkId") Long linkId) {
        findLink(calculationId, linkId);
        topologyLinkService.delete(linkId);
    }

    private TopologyNodeDto toDto(NetworkNode node) {
        return new TopologyNodeDto(node.getId(), node.getCode(), node.getName(), node.getPosX(), node.getPosY());
    }

    private TopologyDeviceDto toDto(EndpointDevice device) {
        return new TopologyDeviceDto(
                device.getId(), device.getType(), device.getCode(), device.getName(), device.getPosX(), device.getPosY());
    }

    private TopologyLinkDto toDto(TopologyLink link) {
        return new TopologyLinkDto(
                link.getId(),
                link.getFromNode() != null ? link.getFromNode().getId() : null,
                link.getToNode() != null ? link.getToNode().getId() : null,
                link.getFromDevice() != null ? link.getFromDevice().getId() : null,
                link.getToDevice() != null ? link.getToDevice().getId() : null,
                link.getLinkType(),
                link.getCableLength(),
                link.getFiberCores(),
                link.getFiberSpliceCount(),
                link.getFiberConnectorCount());
    }

    private void ensureCalculationExists(Long calculationId) {
        calculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calculation not found"));
    }

    private NetworkNode findNode(Long calculationId, Long nodeId) {
        NetworkNode node = networkNodeRepository
                .findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Network node not found"));
        if (!node.getCalculation().getId().equals(calculationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Network node not found");
        }
        return node;
    }

    private EndpointDevice findDevice(Long calculationId, Long deviceId) {
        EndpointDevice device = endpointDeviceRepository
                .findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint device not found"));
        if (!device.getCalculation().getId().equals(calculationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint device not found");
        }
        return device;
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

    private void validateEndpointOwnership(Long calculationId, Long nodeId, Long deviceId) {
        if (nodeId != null) {
            findNode(calculationId, nodeId);
        }
        if (deviceId != null) {
            findDevice(calculationId, deviceId);
        }
    }
}
