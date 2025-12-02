package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.CableType;
import com.kapamejlbka.objectmanager.domain.device.DeviceCableProfile;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.device.SurfaceType;
import com.kapamejlbka.objectmanager.domain.device.repository.CableTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.DeviceCableProfileRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrimaryDataCableSummaryService {

    private static final String UNKNOWN_CABLE_TYPE = "Без классификации";

    private final DeviceCableProfileRepository deviceCableProfileRepository;
    private final CableTypeRepository cableTypeRepository;

    public PrimaryDataCableSummaryService(DeviceCableProfileRepository deviceCableProfileRepository,
                                          CableTypeRepository cableTypeRepository) {
        this.deviceCableProfileRepository = deviceCableProfileRepository;
        this.cableTypeRepository = cableTypeRepository;
    }

    public CableSummaryResult summarize(PrimaryDataSnapshot snapshot) {
        Map<String, CableLengthAccumulator> cableLengthMap = new LinkedHashMap<>();
        EnumMap<CableFunction, Double> functionTotals = new EnumMap<>(CableFunction.class);
        double totalCableLength = 0.0;
        double structureLengthWithoutMaterial = 0.0;
        Set<String> definedConnectionPoints = new HashSet<>();

        if (snapshot == null) {
            return new CableSummaryResult(cableLengthMap, functionTotals, totalCableLength,
                    structureLengthWithoutMaterial, definedConnectionPoints);
        }

        Map<UUID, List<DeviceCableProfile>> cableProfiles = groupProfilesByDeviceType();
        Map<UUID, CableTypeData> cableTypeData = loadCableTypeData();

        if (snapshot.getDeviceGroups() != null) {
            for (PrimaryDataSnapshot.DeviceGroup group : snapshot.getDeviceGroups()) {
                if (group == null) {
                    continue;
                }
                double segmentLength = Math.max(0.0, defaultDouble(group.getDistanceToConnectionPoint()))
                        * Math.max(group.getQuantity(), 0);

                double explicitLength = accumulateExplicitDeviceCables(group, segmentLength, cableLengthMap,
                        cableTypeData, functionTotals);
                if (explicitLength <= 0) {
                    List<DeviceCableProfile> profiles = cableProfiles.get(group.getDeviceTypeId());
                    if (profiles == null || profiles.isEmpty()) {
                        addCableLength(cableLengthMap, UNKNOWN_CABLE_TYPE, segmentLength, true,
                                CableFunction.UNKNOWN, functionTotals);
                        totalCableLength += segmentLength;
                    } else {
                        for (DeviceCableProfile profile : profiles) {
                            if (profile == null || profile.getCableType() == null) {
                                continue;
                            }
                            CableType type = profile.getCableType();
                            String cableName = type.getName();
                            CableFunction function = type.getFunction();
                            if (function == null) {
                                function = CableFunction.SIGNAL;
                            }
                            addCableLength(cableLengthMap, cableName, segmentLength, false, function, functionTotals);
                            totalCableLength += segmentLength;
                        }
                    }
                } else {
                    totalCableLength += explicitLength;
                }
            }
        }

        if (snapshot.getConnectionPoints() != null) {
            for (PrimaryDataSnapshot.ConnectionPoint point : snapshot.getConnectionPoints()) {
                if (point == null) {
                    continue;
                }
                String trimmedName = point.getName() != null ? point.getName().trim() : null;
                if (StringUtils.hasText(trimmedName)) {
                    definedConnectionPoints.add(trimmedName);
                }
                CableTypeData powerCable = resolveCable(point.getPowerCableTypeName(),
                        point.getPowerCableTypeId(), cableTypeData);
                double powerLength = Math.max(0.0, defaultDouble(point.getDistanceToPower()));
                if (powerLength > 0) {
                    CableFunction function = powerCable != null && powerCable.function() != null
                            ? powerCable.function() : CableFunction.UNKNOWN;
                    boolean missingClassification = powerCable == null || powerCable.function() == CableFunction.UNKNOWN;
                    String cableName = powerCable != null && StringUtils.hasText(powerCable.name())
                            ? powerCable.name() : UNKNOWN_CABLE_TYPE;
                    addCableLength(cableLengthMap, cableName,
                            powerLength, missingClassification, function, functionTotals);
                    totalCableLength += powerLength;
                    boolean hasMaterial = point.getLayingMaterialId() != null
                            || StringUtils.hasText(point.getLayingMaterialName());
                    if (!hasMaterial) {
                        SurfaceType surfaceType = SurfaceType.resolve(point.getLayingSurfaceCategory())
                                .orElseGet(() -> SurfaceType.resolve(point.getLayingSurface()).orElse(null));
                        if (surfaceType == SurfaceType.EXISTING_STRUCTURES) {
                            structureLengthWithoutMaterial += powerLength;
                        }
                    }
                }
            }
        }

        return new CableSummaryResult(cableLengthMap, functionTotals, totalCableLength,
                structureLengthWithoutMaterial, definedConnectionPoints);
    }

    private double accumulateExplicitDeviceCables(PrimaryDataSnapshot.DeviceGroup group,
                                                  double segmentLength,
                                                  Map<String, CableLengthAccumulator> cableLengthMap,
                                                  Map<UUID, CableTypeData> cableTypeData,
                                                  EnumMap<CableFunction, Double> functionTotals) {
        if (segmentLength <= 0 || group == null) {
            return 0.0;
        }
        double total = 0.0;
        total += addExplicitCable(group.getSignalCableTypeName(), group.getSignalCableTypeId(), segmentLength,
                cableLengthMap, cableTypeData, functionTotals);
        total += addExplicitCable(group.getLowVoltageCableTypeName(), group.getLowVoltageCableTypeId(), segmentLength,
                cableLengthMap, cableTypeData, functionTotals);
        return total;
    }

    private double addExplicitCable(String storedName,
                                    UUID cableTypeId,
                                    double segmentLength,
                                    Map<String, CableLengthAccumulator> cableLengthMap,
                                    Map<UUID, CableTypeData> cableTypeData,
                                    EnumMap<CableFunction, Double> functionTotals) {
        if (cableTypeId == null && !StringUtils.hasText(storedName)) {
            return 0.0;
        }
        CableTypeData cable = resolveCable(storedName, cableTypeId, cableTypeData);
        String name = cable != null ? cable.name() : (StringUtils.hasText(storedName) ? storedName : UNKNOWN_CABLE_TYPE);
        CableFunction function = cable != null && cable.function() != null ? cable.function() : CableFunction.UNKNOWN;
        boolean missingClassification = cable == null || cable.function() == null
                || cable.function() == CableFunction.UNKNOWN;
        addCableLength(cableLengthMap, name, segmentLength, missingClassification, function, functionTotals);
        return segmentLength;
    }

    private Map<UUID, List<DeviceCableProfile>> groupProfilesByDeviceType() {
        Map<UUID, List<DeviceCableProfile>> map = new HashMap<>();
        deviceCableProfileRepository.findAll().forEach(profile -> {
            if (profile == null || profile.getDeviceType() == null || profile.getDeviceType().getId() == null) {
                return;
            }
            map.computeIfAbsent(profile.getDeviceType().getId(), ignored -> new ArrayList<>())
                    .add(profile);
        });
        return map;
    }

    private Map<UUID, CableTypeData> loadCableTypeData() {
        Map<UUID, CableTypeData> map = new HashMap<>();
        cableTypeRepository.findAll().forEach(type -> {
            if (type != null && type.getId() != null) {
                CableFunction function = type.getFunction();
                if (function == null) {
                    function = CableFunction.SIGNAL;
                }
                map.put(type.getId(), new CableTypeData(type.getName(), function));
            }
        });
        return map;
    }

    private void addCableLength(Map<String, CableLengthAccumulator> totals,
                                String cableName,
                                double length,
                                boolean missingClassification,
                                CableFunction function,
                                EnumMap<CableFunction, Double> functionTotals) {
        if (length <= 0) {
            return;
        }
        String key = StringUtils.hasText(cableName) ? cableName : UNKNOWN_CABLE_TYPE;
        CableLengthAccumulator accumulator = totals.computeIfAbsent(key,
                name -> new CableLengthAccumulator(name, 0.0, missingClassification, function));
        accumulator.add(length);
        if (!missingClassification) {
            accumulator.setClassificationMissing(false);
        }
        CableFunction effectiveFunction = function != null ? function : CableFunction.UNKNOWN;
        functionTotals.merge(effectiveFunction, length, Double::sum);
    }

    private CableTypeData resolveCable(String storedName, UUID cableTypeId, Map<UUID, CableTypeData> cableTypeData) {
        CableTypeData resolved = null;
        if (cableTypeId != null) {
            resolved = cableTypeData.get(cableTypeId);
        }
        if (StringUtils.hasText(storedName)) {
            if (resolved == null) {
                return new CableTypeData(storedName, CableFunction.UNKNOWN);
            }
            return new CableTypeData(storedName, resolved.function());
        }
        return resolved;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    public record CableSummaryResult(Map<String, CableLengthAccumulator> cableLengthMap,
                                     EnumMap<CableFunction, Double> functionTotals,
                                     double totalCableLength,
                                     double structureLengthWithoutMaterial,
                                     Set<String> definedConnectionPoints) {
    }

    private record CableTypeData(String name, CableFunction function) {
    }
}
