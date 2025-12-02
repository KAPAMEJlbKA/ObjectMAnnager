package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.calculation.repository.SystemCalculationRepository;
import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteCreateRequest;
import com.kapamejlbka.objectmanager.domain.topology.dto.InstallationRouteUpdateRequest;
import com.kapamejlbka.objectmanager.domain.topology.repository.InstallationRouteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InstallationRouteService {

    private static final Set<String> SUPPORTED_ROUTE_TYPES =
            Set.of("CORRUGATED_PIPE", "CABLE_CHANNEL", "TRAY_OR_STRUCTURE", "WIRE_ROPE", "BARE_CABLE");
    private static final Set<String> SUPPORTED_FIXING_METHODS = Set.of("ONE_CLIP", "PE_TIES");
    private static final Set<String> SUPPORTED_ORIENTATIONS = Set.of("HORIZONTAL", "VERTICAL");

    private final InstallationRouteRepository installationRouteRepository;
    private final SystemCalculationRepository systemCalculationRepository;

    public InstallationRouteService(
            InstallationRouteRepository installationRouteRepository,
            SystemCalculationRepository systemCalculationRepository) {
        this.installationRouteRepository = installationRouteRepository;
        this.systemCalculationRepository = systemCalculationRepository;
    }

    @Transactional
    public InstallationRoute create(Long calculationId, InstallationRouteCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Installation route data is required");
        }
        InstallationRoute installationRoute = new InstallationRoute();
        installationRoute.setCalculation(getCalculationById(calculationId));
        applyDto(installationRoute, dto);
        LocalDateTime now = LocalDateTime.now();
        installationRoute.setCreatedAt(now);
        installationRoute.setUpdatedAt(now);
        return installationRouteRepository.save(installationRoute);
    }

    @Transactional
    public InstallationRoute update(Long id, InstallationRouteUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Installation route data is required");
        }
        InstallationRoute installationRoute = getById(id);
        applyDto(installationRoute, dto);
        installationRoute.setUpdatedAt(LocalDateTime.now());
        return installationRouteRepository.save(installationRoute);
    }

    public List<InstallationRoute> listByCalculation(Long calculationId) {
        return installationRouteRepository.findByCalculationId(calculationId);
    }

    @Transactional
    public void delete(Long id) {
        InstallationRoute installationRoute = getById(id);
        installationRouteRepository.delete(installationRoute);
    }

    private InstallationRoute getById(Long id) {
        return installationRouteRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Installation route not found: " + id));
    }

    private SystemCalculation getCalculationById(Long calculationId) {
        return systemCalculationRepository
                .findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));
    }

    private void applyDto(InstallationRoute installationRoute, InstallationRouteCreateRequest dto) {
        applyDto(
                installationRoute,
                dto.getName(),
                dto.getRouteType(),
                dto.getMountSurface(),
                dto.getLengthMeters(),
                dto.getOrientation(),
                dto.getFixingMethod());
    }

    private void applyDto(InstallationRoute installationRoute, InstallationRouteUpdateRequest dto) {
        applyDto(
                installationRoute,
                dto.getName(),
                dto.getRouteType(),
                dto.getMountSurface(),
                dto.getLengthMeters(),
                dto.getOrientation(),
                dto.getFixingMethod());
    }

    private void applyDto(
            InstallationRoute installationRoute,
            String name,
            String routeType,
            String mountSurface,
            Double lengthMeters,
            String orientation,
            String fixingMethod) {
        String normalizedName = normalize(name);
        if (!StringUtils.hasText(normalizedName)) {
            throw new IllegalArgumentException("Installation route name is required");
        }
        installationRoute.setName(normalizedName);

        String normalizedRouteType = normalize(routeType);
        if (!StringUtils.hasText(normalizedRouteType)) {
            throw new IllegalArgumentException("Installation route type is required");
        }
        String upperRouteType = normalizedRouteType.toUpperCase();
        if (!SUPPORTED_ROUTE_TYPES.contains(upperRouteType)) {
            throw new IllegalArgumentException("Unsupported installation route type: " + upperRouteType);
        }

        if (lengthMeters == null || lengthMeters <= 0) {
            throw new IllegalArgumentException("Installation route length must be greater than 0");
        }
        installationRoute.setLengthMeters(lengthMeters);

        installationRoute.setMountSurface(normalize(mountSurface));

        if ("CORRUGATED_PIPE".equals(upperRouteType)) {
            String normalizedOrientation = normalize(orientation);
            if (normalizedOrientation != null) {
                String upperOrientation = normalizedOrientation.toUpperCase();
                if (!SUPPORTED_ORIENTATIONS.contains(upperOrientation)) {
                    throw new IllegalArgumentException("Unsupported orientation for corrugated pipe: " + upperOrientation);
                }
                installationRoute.setOrientation(upperOrientation);
            } else {
                installationRoute.setOrientation(null);
            }
        } else {
            installationRoute.setOrientation(null);
        }

        if ("BARE_CABLE".equals(upperRouteType)) {
            String normalizedFixingMethod = normalize(fixingMethod);
            if (!StringUtils.hasText(normalizedFixingMethod)) {
                throw new IllegalArgumentException("Fixing method is required for bare cable routes");
            }
            String upperFixingMethod = normalizedFixingMethod.toUpperCase();
            if (!SUPPORTED_FIXING_METHODS.contains(upperFixingMethod)) {
                throw new IllegalArgumentException("Unsupported fixing method for bare cable: " + upperFixingMethod);
            }
            installationRoute.setFixingMethod(upperFixingMethod);
        } else {
            installationRoute.setFixingMethod(null);
        }

        installationRoute.setRouteType(upperRouteType);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
