package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.CameraInstallationOption;
import com.kapamejlbka.objectmanager.model.DeviceTypeRules;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.model.SurfaceType;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrimaryDataDeviceSummaryService {

    private static final String UNKNOWN_DEVICE_TYPE = "Не указан";

    public DeviceSummaryResult summarize(PrimaryDataSnapshot snapshot) {
        if (snapshot == null || snapshot.getDeviceGroups() == null) {
            return DeviceSummaryResult.empty();
        }
        Map<String, Integer> deviceTypeCounts = new LinkedHashMap<>();
        int totalDevices = 0;
        int unnamedAssignments = 0;
        Set<String> assignedConnectionPoints = new HashSet<>();
        EnumMap<SurfaceType, Integer> cameraCountsBySurface = new EnumMap<>(SurfaceType.class);
        EnumMap<SurfaceType, Integer> adapterCountsBySurface = new EnumMap<>(SurfaceType.class);
        EnumMap<SurfaceType, Integer> plasticBoxCountsBySurface = new EnumMap<>(SurfaceType.class);
        Map<String, MaterialAccumulator> additionalMaterials = new LinkedHashMap<>();
        List<PrimaryDataSummary.CameraDetail> cameraDetails = new ArrayList<>();
        int totalCameras = 0;
        int adapterCameras = 0;
        int plasticBoxCameras = 0;

        for (PrimaryDataSnapshot.DeviceGroup group : snapshot.getDeviceGroups()) {
            if (group == null) {
                continue;
            }
            String typeName = StringUtils.hasText(group.getDeviceTypeName())
                    ? group.getDeviceTypeName()
                    : UNKNOWN_DEVICE_TYPE;
            deviceTypeCounts.merge(typeName, group.getQuantity(), Integer::sum);
            totalDevices += Math.max(group.getQuantity(), 0);

            DeviceTypeRules.lookup(typeName).ifPresent(requirements -> {
                for (DeviceTypeRules.MaterialRequirement requirement : requirements.materialRequirements()) {
                    addMaterial(additionalMaterials, requirement.name(), requirement.unit(),
                            Math.max(group.getQuantity(), 0) * requirement.quantityPerDevice());
                }
            });

            if (isCameraType(group.getDeviceTypeName())) {
                int cameraCount = Math.max(group.getQuantity(), 0);
                totalCameras += cameraCount;
                SurfaceType surfaceType = SurfaceType.resolve(group.getInstallSurfaceCategory())
                        .orElse(SurfaceType.UNKNOWN);
                cameraCountsBySurface.merge(surfaceType, cameraCount, Integer::sum);
                CameraInstallationOption option = CameraInstallationOption.fromCode(group.getCameraAccessory())
                        .orElse(null);
                if (option == CameraInstallationOption.ADAPTER) {
                    adapterCameras += cameraCount;
                    adapterCountsBySurface.merge(surfaceType, cameraCount, Integer::sum);
                } else if (option == CameraInstallationOption.PLASTIC_BOX) {
                    plasticBoxCameras += cameraCount;
                    plasticBoxCountsBySurface.merge(surfaceType, cameraCount, Integer::sum);
                }
                if (cameraCount > 0) {
                    String surfaceLabel = resolveSurfaceLabel(surfaceType, group.getInstallSurfaceCategory());
                    cameraDetails.add(new PrimaryDataSummary.CameraDetail(
                            typeName,
                            cameraCount,
                            group.getInstallLocation(),
                            group.getConnectionPoint(),
                            surfaceLabel,
                            resolveAccessoryLabel(option),
                            group.getCameraViewingDepth()));
                }
            }

            if (!StringUtils.hasText(group.getConnectionPoint())) {
                unnamedAssignments += group.getQuantity();
            }
            String assignment = StringUtils.hasText(group.getConnectionPoint())
                    ? group.getConnectionPoint().trim() : null;
            if (assignment != null) {
                assignedConnectionPoints.add(assignment);
            }
        }

        return new DeviceSummaryResult(deviceTypeCounts,
                totalDevices,
                unnamedAssignments,
                assignedConnectionPoints,
                additionalMaterials,
                cameraCountsBySurface,
                adapterCountsBySurface,
                plasticBoxCountsBySurface,
                totalCameras,
                adapterCameras,
                plasticBoxCameras,
                cameraDetails);
    }

    private boolean isCameraType(String typeName) {
        if (!StringUtils.hasText(typeName)) {
            return false;
        }
        if (DeviceTypeRules.isCamera(typeName)) {
            return true;
        }
        String normalized = Normalizer.normalize(typeName, Normalizer.Form.NFKD).toLowerCase();
        return normalized.contains("камера");
    }

    private String resolveSurfaceLabel(SurfaceType surfaceType, String fallback) {
        if (surfaceType != null && surfaceType != SurfaceType.UNKNOWN) {
            return surfaceType.getDisplayName();
        }
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }
        return null;
    }

    private String resolveAccessoryLabel(CameraInstallationOption option) {
        return option != null ? option.getDisplayName() : "Не указано";
    }

    private void addMaterial(Map<String, MaterialAccumulator> accumulator, String name, String unit, double quantity) {
        if (quantity <= 0 || !StringUtils.hasText(name)) {
            return;
        }
        String key = buildMaterialKey(name, unit);
        MaterialAccumulator entry = accumulator.computeIfAbsent(key,
                ignored -> new MaterialAccumulator(name, unit, 0.0));
        entry.add(quantity);
    }

    private String buildMaterialKey(String name, String unit) {
        String normalizedName = normalizeKey(name);
        String normalizedUnit = normalizeKey(unit);
        return (normalizedName != null ? normalizedName : "") + "|" + (normalizedUnit != null ? normalizedUnit : "");
    }

    private String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return Normalizer.normalize(trimmed, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
    }

    public record DeviceSummaryResult(Map<String, Integer> deviceTypeCounts,
                                      int totalDevices,
                                      int unnamedAssignments,
                                      Set<String> assignedConnectionPoints,
                                      Map<String, MaterialAccumulator> additionalMaterials,
                                      EnumMap<SurfaceType, Integer> cameraCountsBySurface,
                                      EnumMap<SurfaceType, Integer> adapterCountsBySurface,
                                      EnumMap<SurfaceType, Integer> plasticBoxCountsBySurface,
                                      int totalCameras,
                                      int adapterCameras,
                                      int plasticBoxCameras,
                                      List<PrimaryDataSummary.CameraDetail> cameraDetails) {

        static DeviceSummaryResult empty() {
            return new DeviceSummaryResult(new LinkedHashMap<>(),
                    0,
                    0,
                    new HashSet<>(),
                    new HashMap<>(),
                    new EnumMap<>(SurfaceType.class),
                    new EnumMap<>(SurfaceType.class),
                    new EnumMap<>(SurfaceType.class),
                    0,
                    0,
                    0,
                    new ArrayList<>());
        }
    }
}
