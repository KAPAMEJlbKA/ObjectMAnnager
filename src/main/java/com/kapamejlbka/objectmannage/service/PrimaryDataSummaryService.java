package com.kapamejlbka.objectmannage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmannage.model.CableType;
import com.kapamejlbka.objectmannage.model.DeviceCableProfile;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.CableLengthSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.NodeSummary;
import com.kapamejlbka.objectmannage.repository.CableTypeRepository;
import com.kapamejlbka.objectmannage.repository.DeviceCableProfileRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrimaryDataSummaryService {

    private static final String UNKNOWN_DEVICE_TYPE = "Не указан";
    private static final String UNKNOWN_CABLE_TYPE = "Без классификации";

    private final ObjectMapper objectMapper;
    private final DeviceCableProfileRepository deviceCableProfileRepository;
    private final CableTypeRepository cableTypeRepository;

    public PrimaryDataSummaryService(ObjectProvider<ObjectMapper> objectMapperProvider,
                                     DeviceCableProfileRepository deviceCableProfileRepository,
                                     CableTypeRepository cableTypeRepository) {
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        this.deviceCableProfileRepository = deviceCableProfileRepository;
        this.cableTypeRepository = cableTypeRepository;
    }

    public PrimaryDataSummary summarize(String json) {
        if (!StringUtils.hasText(json)) {
            return PrimaryDataSummary.empty();
        }
        PrimaryDataSnapshot snapshot;
        try {
            snapshot = objectMapper.readValue(json, PrimaryDataSnapshot.class);
        } catch (JsonProcessingException e) {
            return PrimaryDataSummary.parseError(e.getOriginalMessage());
        }
        return summarize(snapshot);
    }

    private PrimaryDataSummary summarize(PrimaryDataSnapshot snapshot) {
        if (snapshot == null) {
            return PrimaryDataSummary.empty();
        }
        Map<String, Integer> deviceTypeCounts = new LinkedHashMap<>();
        Map<String, CableLengthAccumulator> cableLengthMap = new LinkedHashMap<>();
        List<NodeSummary> nodeSummaries = new ArrayList<>();

        Map<UUID, List<DeviceCableProfile>> cableProfiles = groupProfilesByDeviceType();
        Map<UUID, String> cableTypeNames = loadCableTypeNames();

        int totalDevices = 0;
        int unnamedAssignments = 0;
        double totalCableLength = 0.0;

        if (snapshot.getDeviceGroups() != null) {
            for (PrimaryDataSnapshot.DeviceGroup group : snapshot.getDeviceGroups()) {
                if (group == null) {
                    continue;
                }
                String typeName = StringUtils.hasText(group.getDeviceTypeName())
                        ? group.getDeviceTypeName()
                        : UNKNOWN_DEVICE_TYPE;
                deviceTypeCounts.merge(typeName, group.getQuantity(), Integer::sum);
                totalDevices += Math.max(group.getQuantity(), 0);

                if (!StringUtils.hasText(group.getConnectionPoint())) {
                    unnamedAssignments += group.getQuantity();
                }

                double segmentLength = Math.max(0.0, defaultDouble(group.getDistanceToConnectionPoint()))
                        * Math.max(group.getQuantity(), 0);

                List<DeviceCableProfile> profiles = cableProfiles.get(group.getDeviceTypeId());
                if (profiles == null || profiles.isEmpty()) {
                    addCableLength(cableLengthMap, UNKNOWN_CABLE_TYPE, segmentLength, true);
                    totalCableLength += segmentLength;
                } else {
                    for (DeviceCableProfile profile : profiles) {
                        if (profile == null || profile.getCableType() == null) {
                            continue;
                        }
                        String cableName = profile.getCableType().getName();
                        addCableLength(cableLengthMap, cableName, segmentLength, false);
                        totalCableLength += segmentLength;
                    }
                }
            }
        }

        if (snapshot.getConnectionPoints() != null) {
            for (PrimaryDataSnapshot.ConnectionPoint point : snapshot.getConnectionPoints()) {
                if (point == null) {
                    continue;
                }
                String name = StringUtils.hasText(point.getName()) ? point.getName() : "Без названия";
                String elementName = point.getMountingElementName();
                Double distanceToPower = point.getDistanceToPower();
                String powerCableName = resolveCableName(point.getPowerCableTypeName(),
                        point.getPowerCableTypeId(), cableTypeNames);
                String layingMethod = point.getLayingMethod();
                nodeSummaries.add(new NodeSummary(name, elementName, distanceToPower, powerCableName, layingMethod));

                double powerLength = Math.max(0.0, defaultDouble(distanceToPower));
                if (powerLength > 0) {
                    addCableLength(cableLengthMap, powerCableName != null ? powerCableName : UNKNOWN_CABLE_TYPE,
                            powerLength, !StringUtils.hasText(powerCableName));
                    totalCableLength += powerLength;
                }
            }
        }

        List<DeviceTypeSummary> deviceSummaries = new ArrayList<>();
        deviceTypeCounts.forEach((name, quantity) ->
                deviceSummaries.add(new DeviceTypeSummary(name, quantity != null ? quantity : 0)));
        deviceSummaries.sort(Comparator.comparing(DeviceTypeSummary::getDeviceTypeName,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        List<CableLengthSummary> cableSummaries = new ArrayList<>();
        cableLengthMap.values().stream()
                .sorted(Comparator.comparing(CableLengthAccumulator::name,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(acc -> cableSummaries.add(new CableLengthSummary(
                        acc.name(),
                        acc.totalLength(),
                        acc.classificationMissing())));

        PrimaryDataSummary.Builder builder = PrimaryDataSummary.builder()
                .withHasData(true)
                .withTotalDeviceCount(totalDevices)
                .withTotalNodes(nodeSummaries.size())
                .withUnnamedConnectionAssignments(unnamedAssignments)
                .withDeclaredConnectionAssignments(snapshot.getTotalConnectionPoints())
                .withTotalCableLength(totalCableLength);

        deviceSummaries.forEach(builder::addDeviceTypeSummary);
        cableSummaries.forEach(builder::addCableLengthSummary);
        nodeSummaries.forEach(builder::addNodeSummary);
        return builder.build();
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

    private Map<UUID, String> loadCableTypeNames() {
        Map<UUID, String> map = new HashMap<>();
        cableTypeRepository.findAll().forEach(type -> {
            if (type != null && type.getId() != null) {
                map.put(type.getId(), type.getName());
            }
        });
        return map;
    }

    private void addCableLength(Map<String, CableLengthAccumulator> totals,
                                String cableName,
                                double length,
                                boolean missingClassification) {
        if (length <= 0) {
            return;
        }
        String key = StringUtils.hasText(cableName) ? cableName : UNKNOWN_CABLE_TYPE;
        CableLengthAccumulator accumulator = totals.computeIfAbsent(key,
                name -> new CableLengthAccumulator(name, 0.0, missingClassification));
        accumulator.add(length);
        if (!missingClassification) {
            accumulator.setClassificationMissing(false);
        }
    }

    private String resolveCableName(String storedName, UUID cableTypeId, Map<UUID, String> cableTypeNames) {
        if (StringUtils.hasText(storedName)) {
            return storedName;
        }
        if (cableTypeId != null) {
            return cableTypeNames.get(cableTypeId);
        }
        return null;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private static class CableLengthAccumulator {
        private final String name;
        private double totalLength;
        private boolean classificationMissing;

        CableLengthAccumulator(String name, double totalLength, boolean classificationMissing) {
            this.name = name;
            this.totalLength = totalLength;
            this.classificationMissing = classificationMissing;
        }

        void add(double value) {
            this.totalLength += value;
        }

        void setClassificationMissing(boolean classificationMissing) {
            this.classificationMissing = classificationMissing;
        }

        String name() {
            return name;
        }

        double totalLength() {
            return totalLength;
        }

        boolean classificationMissing() {
            return classificationMissing;
        }
    }
}
