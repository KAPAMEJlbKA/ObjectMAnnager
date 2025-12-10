package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkLengthUpdate;
import com.kapamejlbka.objectmanager.domain.topology.dto.TopologyLinkUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.repository.TopologyLinkRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TopologyLinkService {

    private static final Set<String> SUPPORTED_LINK_TYPES = Set.of("UTP", "FIBER", "POWER", "WIFI");

    private final TopologyLinkRepository topologyLinkRepository;
    private final SystemCalculationRepository systemCalculationRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final EndpointDeviceRepository endpointDeviceRepository;

    public TopologyLinkService(
            TopologyLinkRepository topologyLinkRepository,
            SystemCalculationRepository systemCalculationRepository,
            NetworkNodeRepository networkNodeRepository,
            EndpointDeviceRepository endpointDeviceRepository) {
        this.topologyLinkRepository = topologyLinkRepository;
        this.systemCalculationRepository = systemCalculationRepository;
        this.networkNodeRepository = networkNodeRepository;
        this.endpointDeviceRepository = endpointDeviceRepository;
    }

    @Transactional
    public TopologyLink create(Long calculationId, TopologyLinkCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Topology link data is required");
        }
        TopologyLink topologyLink = new TopologyLink();
        topologyLink.setCalculation(getCalculationById(calculationId));
        applyDto(topologyLink, dto);
        LocalDateTime now = LocalDateTime.now();
        topologyLink.setCreatedAt(now);
        topologyLink.setUpdatedAt(now);
        return topologyLinkRepository.save(topologyLink);
    }

    @Transactional
    public TopologyLink update(Long id, TopologyLinkUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Topology link data is required");
        }
        TopologyLink topologyLink = getById(id);
        applyDto(topologyLink, dto);
        topologyLink.setUpdatedAt(LocalDateTime.now());
        return topologyLinkRepository.save(topologyLink);
    }

    public List<TopologyLink> listByCalculation(Long calculationId) {
        return topologyLinkRepository.findByCalculationId(calculationId);
    }

    @Transactional
    public void delete(Long id) {
        TopologyLink topologyLink = getById(id);
        topologyLinkRepository.delete(topologyLink);
    }

    @Transactional
    public void updateCableLengths(Long calculationId, List<TopologyLinkLengthUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        for (TopologyLinkLengthUpdate update : updates) {
            if (update.getId() == null) {
                continue;
            }
            TopologyLink link = getById(update.getId());
            if (!link.getCalculation().getId().equals(calculationId)) {
                throw new IllegalArgumentException("Link does not belong to calculation");
            }
            if (!SUPPORTED_LINK_TYPES.contains(link.getLinkType())) {
                throw new IllegalArgumentException("Unsupported link type: " + link.getLinkType());
            }
            Double length = update.getLength();
            if (length != null && length < 0) {
                throw new IllegalArgumentException("Cable length cannot be negative");
            }
            link.setCableLength(length);
            link.setUpdatedAt(LocalDateTime.now());
            topologyLinkRepository.save(link);
        }
    }

    private void applyDto(TopologyLink topologyLink, TopologyLinkCreateRequest dto) {
        applyDto(
                topologyLink,
                dto.getFromNodeId(),
                dto.getToNodeId(),
                dto.getFromDeviceId(),
                dto.getToDeviceId(),
                dto.getLinkType(),
                dto.getCableLength(),
                dto.getWireless(),
                dto.getFiberCores(),
                dto.getFiberSpliceCount(),
                dto.getFiberConnectorCount(),
                dto.getPowerSourceDescription());
    }

    private void applyDto(TopologyLink topologyLink, TopologyLinkUpdateRequest dto) {
        applyDto(
                topologyLink,
                dto.getFromNodeId(),
                dto.getToNodeId(),
                dto.getFromDeviceId(),
                dto.getToDeviceId(),
                dto.getLinkType(),
                dto.getCableLength(),
                dto.getWireless(),
                dto.getFiberCores(),
                dto.getFiberSpliceCount(),
                dto.getFiberConnectorCount(),
                dto.getPowerSourceDescription());
    }

    private void applyDto(
            TopologyLink topologyLink,
            Long fromNodeId,
            Long toNodeId,
            Long fromDeviceId,
            Long toDeviceId,
            String linkType,
            Double cableLength,
            Boolean isWireless,
            Integer fiberCores,
            Integer fiberSpliceCount,
            Integer fiberConnectorCount,
            String powerSourceDescription) {
        topologyLink.setFromNode(fromNodeId != null ? getNetworkNode(fromNodeId) : null);
        topologyLink.setToNode(toNodeId != null ? getNetworkNode(toNodeId) : null);
        topologyLink.setFromDevice(fromDeviceId != null ? getEndpointDevice(fromDeviceId) : null);
        topologyLink.setToDevice(toDeviceId != null ? getEndpointDevice(toDeviceId) : null);
        validateEndpoints(topologyLink);

        String normalizedType = normalize(linkType);
        if (!StringUtils.hasText(normalizedType)) {
            throw new IllegalArgumentException("Topology link type is required");
        }
        String upperType = normalizedType.toUpperCase();
        if (!SUPPORTED_LINK_TYPES.contains(upperType)) {
            throw new IllegalArgumentException("Unsupported topology link type: " + upperType);
        }

        Double normalizedCableLength = cableLength;
        if (normalizedCableLength != null && normalizedCableLength <= 0) {
            throw new IllegalArgumentException("Cable length must be greater than 0");
        }

        if ("POWER".equals(upperType)) {
            if (normalizedCableLength == null) {
                throw new IllegalArgumentException("Cable length is required for POWER links");
            }
            String normalizedPowerSource = normalize(powerSourceDescription);
            if (!StringUtils.hasText(normalizedPowerSource)) {
                throw new IllegalArgumentException("Power source description is required for POWER links");
            }
            topologyLink.setPowerSourceDescription(normalizedPowerSource);
        } else {
            topologyLink.setPowerSourceDescription(normalize(powerSourceDescription));
        }

        if ("FIBER".equals(upperType)) {
            if (fiberCores == null || fiberCores <= 0) {
                throw new IllegalArgumentException("Fiber cores must be greater than 0 for FIBER links");
            }
            if (fiberCores < 4 || fiberCores > 32) {
                throw new IllegalArgumentException("Fiber cores must be between 4 and 32");
            }
            if (fiberSpliceCount != null && fiberSpliceCount < 0) {
                throw new IllegalArgumentException("Fiber splice count cannot be negative");
            }
            if (fiberConnectorCount != null && fiberConnectorCount < 0) {
                throw new IllegalArgumentException("Fiber connector count cannot be negative");
            }
            topologyLink.setFiberCores(fiberCores);
            topologyLink.setFiberSpliceCount(fiberSpliceCount);
            topologyLink.setFiberConnectorCount(fiberConnectorCount);
        } else {
            topologyLink.setFiberCores(null);
            topologyLink.setFiberSpliceCount(null);
            topologyLink.setFiberConnectorCount(null);
        }

        topologyLink.setCableLength(normalizedCableLength);
        topologyLink.setLinkType(upperType);
        topologyLink.setWireless(Boolean.TRUE.equals(isWireless) || "WIFI".equals(upperType));
    }

    private void validateEndpoints(TopologyLink topologyLink) {
        int endpointCount = (topologyLink.getFromNode() != null ? 1 : 0)
                + (topologyLink.getToNode() != null ? 1 : 0)
                + (topologyLink.getFromDevice() != null ? 1 : 0)
                + (topologyLink.getToDevice() != null ? 1 : 0);
        if (endpointCount != 2) {
            throw new IllegalArgumentException("Topology link must have exactly two endpoints");
        }
        boolean hasFrom = topologyLink.getFromNode() != null || topologyLink.getFromDevice() != null;
        boolean hasTo = topologyLink.getToNode() != null || topologyLink.getToDevice() != null;
        if (!hasFrom || !hasTo) {
            throw new IllegalArgumentException("Topology link must include both start and end points");
        }
    }

    private TopologyLink getById(Long id) {
        return topologyLinkRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topology link not found: " + id));
    }

    private SystemCalculation getCalculationById(Long calculationId) {
        return systemCalculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));
    }

    private NetworkNode getNetworkNode(Long id) {
        return networkNodeRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Network node not found: " + id));
    }

    private EndpointDevice getEndpointDevice(Long id) {
        return endpointDeviceRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Endpoint device not found: " + id));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
