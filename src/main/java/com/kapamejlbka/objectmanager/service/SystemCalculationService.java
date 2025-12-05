package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationCreateRequest;
import com.kapamejlbka.objectmanager.domain.calculation.dto.SystemCalculationUpdateRequest;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.calcengine.CalculationEngine;
import com.kapamejlbka.objectmanager.domain.calcengine.CalculationResult;
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
    private final CalculationEngine calculationEngine;

    public SystemCalculationService(
            SystemCalculationRepository systemCalculationRepository,
            SiteRepository siteRepository,
            CalculationEngine calculationEngine) {
        this.systemCalculationRepository = systemCalculationRepository;
        this.siteRepository = siteRepository;
        this.calculationEngine = calculationEngine;
    }

    @Transactional
    public SystemCalculation create(Long siteId, SystemCalculationCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("System calculation data is required");
        }
        SystemCalculation existing = systemCalculationRepository.findFirstBySiteId(siteId).orElse(null);
        if (existing != null) {
            return existing;
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
    public SystemCalculation ensureForSite(Long siteId) {
        return systemCalculationRepository.findFirstBySiteId(siteId)
                .orElseGet(() -> {
                    SystemCalculationCreateRequest dto = new SystemCalculationCreateRequest();
                    dto.setStatus("DRAFT");
                    return create(siteId, dto);
                });
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

    public CalculationResult runCalculation(Long calculationId) {
        return calculationEngine.calculate(calculationId);
    }

    private void applyDto(SystemCalculation calculation, SystemCalculationCreateRequest dto) {
        String name = normalize(dto.getName());
        if (!StringUtils.hasText(name) && calculation.getSite() != null) {
            name = calculation.getSite().getName() + " — данные по объекту";
        }
        calculation.setName(StringUtils.hasText(name) ? name : "Заполнение объекта");
        calculation.setDescription(normalize(dto.getDescription()));
        String systemType = normalize(dto.getSystemType());
        calculation.setSystemType(StringUtils.hasText(systemType) ? systemType : "GENERAL");
        String status = normalize(dto.getStatus());
        if (!StringUtils.hasText(status)) {
            status = calculation.getStatus();
        }
        calculation.setStatus(StringUtils.hasText(status) ? status : "DRAFT");
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
