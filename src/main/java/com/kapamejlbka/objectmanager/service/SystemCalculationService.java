package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationCreateRequest;
import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationUpdateRequest;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.customer.Site;
import com.kapamejlbka.objectmanager.domain.customer.repository.SiteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemCalculationService {

    private final SystemCalculationRepository systemCalculationRepository;
    private final SiteRepository siteRepository;

    public SystemCalculationService(
            SystemCalculationRepository systemCalculationRepository, SiteRepository siteRepository) {
        this.systemCalculationRepository = systemCalculationRepository;
        this.siteRepository = siteRepository;
    }

    @Transactional
    public SystemCalculation create(Long siteId, SystemCalculationCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("System calculation data is required");
        }
        SystemCalculation calculation = new SystemCalculation();
        calculation.setSite(getSiteById(siteId));
        applyDto(calculation, dto);
        LocalDateTime now = LocalDateTime.now();
        calculation.setCreatedAt(now);
        calculation.setUpdatedAt(now);
        return systemCalculationRepository.save(calculation);
    }

    @Transactional
    public SystemCalculation update(Long id, SystemCalculationUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("System calculation data is required");
        }
        SystemCalculation calculation = getById(id);
        applyDto(calculation, dto);
        calculation.setUpdatedAt(LocalDateTime.now());
        return systemCalculationRepository.save(calculation);
    }

    public SystemCalculation getById(Long id) {
        return systemCalculationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System calculation not found: " + id));
    }

    public List<SystemCalculation> findBySite(Long siteId) {
        return systemCalculationRepository.findBySiteId(siteId);
    }

    @Transactional
    public void changeStatus(Long id, String status) {
        SystemCalculation calculation = getById(id);
        String normalizedStatus = normalize(status);
        if (!StringUtils.hasText(normalizedStatus)) {
            throw new IllegalArgumentException("System calculation status is required");
        }
        calculation.setStatus(normalizedStatus);
        calculation.setUpdatedAt(LocalDateTime.now());
        systemCalculationRepository.save(calculation);
    }

    private void applyDto(SystemCalculation calculation, SystemCalculationCreateRequest dto) {
        String name = normalize(dto.getName());
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("System calculation name is required");
        }
        String systemType = normalize(dto.getSystemType());
        if (!StringUtils.hasText(systemType)) {
            throw new IllegalArgumentException("System calculation type is required");
        }
        String status = normalize(dto.getStatus());
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("System calculation status is required");
        }
        calculation.setName(name);
        calculation.setDescription(normalize(dto.getDescription()));
        calculation.setSystemType(systemType);
        calculation.setStatus(status);
    }

    private Site getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found: " + siteId));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
