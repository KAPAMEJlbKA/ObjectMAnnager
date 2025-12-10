package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeCreateRequest;
import com.kapamejlbka.objectmanager.domain.device.dto.NetworkNodeUpdateRequest;
import com.kapamejlbka.objectmanager.domain.device.repository.NetworkNodeRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NetworkNodeService {

    private final NetworkNodeRepository networkNodeRepository;
    private final SystemCalculationRepository systemCalculationRepository;

    public NetworkNodeService(
            NetworkNodeRepository networkNodeRepository,
            SystemCalculationRepository systemCalculationRepository) {
        this.networkNodeRepository = networkNodeRepository;
        this.systemCalculationRepository = systemCalculationRepository;
    }

    @Transactional
    public NetworkNode create(Long calculationId, NetworkNodeCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Network node data is required");
        }
        NetworkNode networkNode = new NetworkNode();
        networkNode.setCalculation(getCalculationById(calculationId));
        applyDto(networkNode, dto);
        LocalDateTime now = LocalDateTime.now();
        networkNode.setCreatedAt(now);
        networkNode.setUpdatedAt(now);
        return networkNodeRepository.save(networkNode);
    }

    @Transactional
    public NetworkNode update(Long id, NetworkNodeUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Network node data is required");
        }
        NetworkNode networkNode = getById(id);
        applyDto(networkNode, dto);
        networkNode.setUpdatedAt(LocalDateTime.now());
        return networkNodeRepository.save(networkNode);
    }

    public List<NetworkNode> listByCalculation(Long calculationId) {
        return networkNodeRepository.findByCalculationId(calculationId);
    }

    public long countByCalculation(Long calculationId) {
        if (calculationId == null) {
            return 0;
        }
        return networkNodeRepository.countByCalculationId(calculationId);
    }

    public long countBySite(Long siteId) {
        if (siteId == null) {
            return 0;
        }
        return networkNodeRepository.countByCalculationSiteId(siteId);
    }

    @Transactional
    public void delete(Long id) {
        NetworkNode networkNode = getById(id);
        networkNodeRepository.delete(networkNode);
    }

    private NetworkNode getById(Long id) {
        return networkNodeRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Network node not found: " + id));
    }

    private SystemCalculation getCalculationById(Long calculationId) {
        return systemCalculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));
    }

    private void applyDto(NetworkNode networkNode, NetworkNodeCreateRequest dto) {
        applyDto(
                networkNode,
                dto.getCode(),
                dto.getName(),
                dto.getMountSurface(),
                dto.getCabinetSize(),
                dto.getCabinetSizeAuto(),
                dto.getBaseCircuitBreakers(),
                dto.getExtraCircuitBreakers(),
                dto.getBaseSockets(),
                dto.getExtraSockets(),
                dto.getIncomingLinesCount());
    }

    private void applyDto(NetworkNode networkNode, NetworkNodeUpdateRequest dto) {
        applyDto(
                networkNode,
                dto.getCode(),
                dto.getName(),
                dto.getMountSurface(),
                dto.getCabinetSize(),
                dto.getCabinetSizeAuto(),
                dto.getBaseCircuitBreakers(),
                dto.getExtraCircuitBreakers(),
                dto.getBaseSockets(),
                dto.getExtraSockets(),
                dto.getIncomingLinesCount());
    }

    private void applyDto(
            NetworkNode networkNode,
            String code,
            String name,
            String mountSurface,
            Integer cabinetSize,
            Boolean cabinetSizeAuto,
            Integer baseCircuitBreakers,
            Integer extraCircuitBreakers,
            Integer baseSockets,
            Integer extraSockets,
            Integer incomingLinesCount) {
        String normalizedCode = normalize(code);
        if (!StringUtils.hasText(normalizedCode)) {
            throw new IllegalArgumentException("Network node code is required");
        }
        String normalizedName = normalize(name);
        if (!StringUtils.hasText(normalizedName)) {
            throw new IllegalArgumentException("Network node name is required");
        }
        if (baseCircuitBreakers == null || baseCircuitBreakers < 1) {
            throw new IllegalArgumentException("Base circuit breakers must be at least 1");
        }
        if (baseSockets == null || baseSockets < 1) {
            throw new IllegalArgumentException("Base sockets must be at least 1");
        }
        networkNode.setCode(normalizedCode);
        networkNode.setName(normalizedName);
        networkNode.setMountSurface(normalize(mountSurface));
        networkNode.setCabinetSize(cabinetSize);
        networkNode.setCabinetSizeAuto(Boolean.TRUE.equals(cabinetSizeAuto));
        networkNode.setBaseCircuitBreakers(baseCircuitBreakers);
        networkNode.setExtraCircuitBreakers(extraCircuitBreakers != null ? extraCircuitBreakers : 0);
        networkNode.setBaseSockets(baseSockets);
        networkNode.setExtraSockets(extraSockets != null ? extraSockets : 0);
        networkNode.setIncomingLinesCount(incomingLinesCount);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
