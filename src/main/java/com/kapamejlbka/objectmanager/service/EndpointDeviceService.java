package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.dto.EndpointDeviceCreateRequest;
import com.kapamejlbka.objectmanager.domain.device.dto.EndpointDeviceUpdateRequest;
import com.kapamejlbka.objectmanager.domain.device.repository.EndpointDeviceRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EndpointDeviceService {

    private final EndpointDeviceRepository endpointDeviceRepository;
    private final SystemCalculationRepository systemCalculationRepository;

    public EndpointDeviceService(
            EndpointDeviceRepository endpointDeviceRepository,
            SystemCalculationRepository systemCalculationRepository) {
        this.endpointDeviceRepository = endpointDeviceRepository;
        this.systemCalculationRepository = systemCalculationRepository;
    }

    @Transactional
    public EndpointDevice create(Long calculationId, EndpointDeviceCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Endpoint device data is required");
        }
        EndpointDevice endpointDevice = new EndpointDevice();
        endpointDevice.setCalculation(getCalculationById(calculationId));
        applyDto(endpointDevice, dto);
        LocalDateTime now = LocalDateTime.now();
        endpointDevice.setCreatedAt(now);
        endpointDevice.setUpdatedAt(now);
        return endpointDeviceRepository.save(endpointDevice);
    }

    @Transactional
    public EndpointDevice update(Long id, EndpointDeviceUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Endpoint device data is required");
        }
        EndpointDevice endpointDevice = getById(id);
        applyDto(endpointDevice, dto);
        endpointDevice.setUpdatedAt(LocalDateTime.now());
        return endpointDeviceRepository.save(endpointDevice);
    }

    public List<EndpointDevice> listByCalculation(Long calculationId) {
        return endpointDeviceRepository.findByCalculationId(calculationId);
    }

    @Transactional
    public void delete(Long id) {
        EndpointDevice endpointDevice = getById(id);
        endpointDeviceRepository.delete(endpointDevice);
    }

    private EndpointDevice getById(Long id) {
        return endpointDeviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Endpoint device not found: " + id));
    }

    private SystemCalculation getCalculationById(Long calculationId) {
        return systemCalculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));
    }

    private void applyDto(EndpointDevice endpointDevice, EndpointDeviceCreateRequest dto) {
        applyDto(
                endpointDevice,
                dto.getType(),
                dto.getCode(),
                dto.getName(),
                dto.getLocationDescription(),
                dto.getViewDepth(),
                dto.getMountSurface());
    }

    private void applyDto(EndpointDevice endpointDevice, EndpointDeviceUpdateRequest dto) {
        applyDto(
                endpointDevice,
                dto.getType(),
                dto.getCode(),
                dto.getName(),
                dto.getLocationDescription(),
                dto.getViewDepth(),
                dto.getMountSurface());
    }

    private void applyDto(
            EndpointDevice endpointDevice,
            String type,
            String code,
            String name,
            String locationDescription,
            Double viewDepth,
            String mountSurface) {
        String normalizedType = normalize(type);
        if (!StringUtils.hasText(normalizedType)) {
            throw new IllegalArgumentException("Endpoint device type is required");
        }
        String normalizedCode = normalize(code);
        if (!StringUtils.hasText(normalizedCode)) {
            throw new IllegalArgumentException("Endpoint device code is required");
        }
        String normalizedName = normalize(name);
        if (!StringUtils.hasText(normalizedName)) {
            throw new IllegalArgumentException("Endpoint device name is required");
        }
        endpointDevice.setType(normalizedType);
        endpointDevice.setCode(normalizedCode);
        endpointDevice.setName(normalizedName);
        endpointDevice.setLocationDescription(normalize(locationDescription));
        endpointDevice.setViewDepth(viewDepth);
        endpointDevice.setMountSurface(normalize(mountSurface));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
