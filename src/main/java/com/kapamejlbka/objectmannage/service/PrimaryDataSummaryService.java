package com.kapamejlbka.objectmannage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapamejlbka.objectmannage.model.CableFunction;
import com.kapamejlbka.objectmannage.model.CableType;
import com.kapamejlbka.objectmannage.model.CameraInstallationOption;
import com.kapamejlbka.objectmannage.model.DeviceCableProfile;
import com.kapamejlbka.objectmannage.model.DeviceTypeRules;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.CableFunctionSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.CableLengthSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.NodeSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.MaterialUsageSummary;
import com.kapamejlbka.objectmannage.model.SurfaceType;
import com.kapamejlbka.objectmannage.repository.CableTypeRepository;
import com.kapamejlbka.objectmannage.repository.DeviceCableProfileRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private final ApplicationSettingsService applicationSettingsService;

    public PrimaryDataSummaryService(ObjectProvider<ObjectMapper> objectMapperProvider,
                                     DeviceCableProfileRepository deviceCableProfileRepository,
                                     CableTypeRepository cableTypeRepository,
                                     ApplicationSettingsService applicationSettingsService) {
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        this.deviceCableProfileRepository = deviceCableProfileRepository;
        this.cableTypeRepository = cableTypeRepository;
        this.applicationSettingsService = applicationSettingsService;
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
        List<NodeContext> nodeContexts = new ArrayList<>();
        Map<String, NodeContext> nodeContextsByNormalizedName = new LinkedHashMap<>();
        Map<String, MaterialAccumulator> additionalMaterials = new LinkedHashMap<>();
        Map<String, MaterialAccumulator> overallMaterialTotals = new LinkedHashMap<>();
        Map<String, MaterialAccumulator> mountingElementTotals = new LinkedHashMap<>();
        EnumMap<SurfaceType, Integer> cameraCountsBySurface = new EnumMap<>(SurfaceType.class);
        EnumMap<SurfaceType, Integer> adapterCountsBySurface = new EnumMap<>(SurfaceType.class);
        EnumMap<SurfaceType, Integer> plasticBoxCountsBySurface = new EnumMap<>(SurfaceType.class);
        List<PrimaryDataSummary.CameraDetail> cameraDetails = new ArrayList<>();

        Map<UUID, List<DeviceCableProfile>> cableProfiles = groupProfilesByDeviceType();
        Map<UUID, CableTypeData> cableTypeData = loadCableTypeData();

        int totalDevices = 0;
        int unnamedAssignments = 0;
        double totalCableLength = 0.0;
        EnumMap<CableFunction, Double> functionTotals = new EnumMap<>(CableFunction.class);
        Set<String> assignedConnectionPoints = new HashSet<>();
        Set<String> definedConnectionPoints = new HashSet<>();
        int totalCameras = 0;
        int adapterCameras = 0;
        int plasticBoxCameras = 0;
        int boxNodes = 0;
        double structureLengthWithoutMaterial = 0.0;
        int unnamedNodeIndex = 1;

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
                String displayName = StringUtils.hasText(trimmedName) ? trimmedName : "Без названия";
                String normalizedName = normalizeKey(trimmedName);
                CableTypeData powerCable = resolveCable(point.getPowerCableTypeName(),
                        point.getPowerCableTypeId(), cableTypeData);
                NodeContext context = new NodeContext(point,
                        displayName,
                        normalizedName,
                        powerCable != null ? powerCable.name() : null);
                nodeContexts.add(context);
                String normalizedDisplayName = normalizeKey(displayName);
                if (normalizedName != null) {
                    nodeContextsByNormalizedName.putIfAbsent(normalizedName, context);
                }
                if (normalizedDisplayName != null) {
                    nodeContextsByNormalizedName.putIfAbsent(normalizedDisplayName, context);
                }
                if (normalizedName == null && normalizedDisplayName == null) {
                    nodeContextsByNormalizedName.putIfAbsent("__unnamed__" + unnamedNodeIndex++, context);
                }

                String elementName = point.getMountingElementName();
                if (isBoxNode(elementName)) {
                    boxNodes++;
                }

                Double distanceToPower = point.getDistanceToPower();
                double powerLength = Math.max(0.0, defaultDouble(distanceToPower));
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
                    SurfaceType surfaceType = SurfaceType.resolve(point.getLayingSurfaceCategory())
                            .orElseGet(() -> SurfaceType.resolve(point.getLayingSurface()).orElse(null));
                    if (!hasMaterial && surfaceType == SurfaceType.EXISTING_STRUCTURES) {
                        structureLengthWithoutMaterial += powerLength;
                    }
                    if (hasMaterial) {
                        String unit = trimText(point.getLayingMaterialUnit());
                        if (!StringUtils.hasText(unit)) {
                            unit = "м";
                        }
                        String surfaceLabel = resolveSurfaceLabel(surfaceType, point.getLayingSurface());
                        addNodeMaterial(context,
                                "Прокладка питания",
                                point.getLayingMaterialName(),
                                formatQuantity(powerLength, unit),
                                powerLength,
                                unit,
                                surfaceLabel,
                                overallMaterialTotals);
                    }
                }
            }
        }

        Map<String, String> labelToNodeMap = mapGroupLabelsToNodes(snapshot.getDeviceGroups());
        if (snapshot.getMaterialGroups() != null) {
            for (PrimaryDataSnapshot.MaterialGroup group : snapshot.getMaterialGroups()) {
                if (group == null || group.getMaterials() == null) {
                    continue;
                }
                String label = determineGroupLabel(group.getGroupLabel(), group.getGroupName());
                NodeContext target = resolveNodeForGroup(label, labelToNodeMap, nodeContextsByNormalizedName);
                for (PrimaryDataSnapshot.MaterialUsage usage : group.getMaterials()) {
                    if (usage == null) {
                        continue;
                    }
                    String name = trimText(usage.getMaterialName());
                    String amountWithUnit = combineAmountWithUnit(usage.getAmount(), usage.getUnit());
                    String surfaceLabel = resolveSurfaceLabel(
                            SurfaceType.resolve(usage.getLayingSurfaceCategory()).orElse(null),
                            usage.getLayingSurface());
                    boolean empty = !StringUtils.hasText(name)
                            && !StringUtils.hasText(amountWithUnit)
                            && !StringUtils.hasText(surfaceLabel);
                    if (empty) {
                        continue;
                    }
                    Double quantity = null;
                    double numeric = parseLength(usage.getAmount());
                    if (numeric > 0) {
                        quantity = numeric;
                    }
                    String unit = resolveUnit(usage.getUnit(), usage.getAmount());
                    if (target != null) {
                        addNodeMaterial(target,
                                label,
                                name,
                                amountWithUnit,
                                quantity,
                                unit,
                                surfaceLabel,
                                overallMaterialTotals);
                    } else if (quantity != null && StringUtils.hasText(name)) {
                        addMaterial(overallMaterialTotals, name, unit, quantity);
                    }
                }
            }
        }

        if (snapshot.getMountingElements() != null) {
            for (PrimaryDataSnapshot.MountingRequirement requirement : snapshot.getMountingElements()) {
                if (requirement == null) {
                    continue;
                }
                String elementName = trimText(requirement.getElementName());
                double quantity = parseLength(requirement.getQuantity());
                String unit = resolveUnit(null, requirement.getQuantity());
                if (quantity > 0 && StringUtils.hasText(elementName)) {
                    addMaterial(mountingElementTotals, elementName, unit, quantity);
                }
                if (requirement.getMaterials() != null) {
                    for (PrimaryDataSnapshot.MountingMaterial material : requirement.getMaterials()) {
                        if (material == null) {
                            continue;
                        }
                        String materialName = trimText(material.getMaterialName());
                        double materialQuantity = parseLength(material.getAmount());
                        String materialUnit = resolveUnit(material.getUnit(), material.getAmount());
                        if (materialQuantity > 0 && StringUtils.hasText(materialName)) {
                            addMaterial(overallMaterialTotals, materialName, materialUnit, materialQuantity);
                        }
                    }
                }
            }
        }

        List<NodeSummary> nodeSummaries = buildNodeSummaries(nodeContexts);
        List<DeviceTypeSummary> deviceSummaries = new ArrayList<>();
        deviceTypeCounts.forEach((name, quantity) ->
                deviceSummaries.add(new DeviceTypeSummary(name, quantity != null ? quantity : 0)));
        deviceSummaries.sort(Comparator.comparing(DeviceTypeSummary::getDeviceTypeName,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        String deviceBreakdown = deviceSummaries.stream()
                .map(summary -> String.format("%s — %d", summary.getDeviceTypeName(), summary.getQuantity()))
                .reduce((left, right) -> left + ", " + right)
                .orElse(null);

        List<CableLengthSummary> cableSummaries = new ArrayList<>();
        cableLengthMap.values().stream()
                .sorted(Comparator.comparing(CableLengthAccumulator::name,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(acc -> cableSummaries.add(new CableLengthSummary(
                        acc.name(),
                        acc.totalLength(),
                        acc.classificationMissing())));

        List<CableFunctionSummary> functionSummaries = new ArrayList<>();
        functionTotals.forEach((function, total) -> {
            if (total != null && total > 0) {
                functionSummaries.add(new CableFunctionSummary(function.getDisplayName(), total));
            }
        });
        functionSummaries.sort(Comparator.comparing(CableFunctionSummary::getFunctionName));

        int recordedConnectionPoints = Math.max(0, snapshot.getTotalConnectionPoints());
        if (recordedConnectionPoints == 0 && !definedConnectionPoints.isEmpty()) {
            recordedConnectionPoints = definedConnectionPoints.size();
        }
        if (recordedConnectionPoints == 0 && !assignedConnectionPoints.isEmpty()) {
            recordedConnectionPoints = assignedConnectionPoints.size();
        }
        Integer declaredAssignments = recordedConnectionPoints > 0 ? recordedConnectionPoints : null;

        MaterialLengthStats materialLengths = collectMaterialLengths(snapshot);
        accumulateCameraMaterials(additionalMaterials,
                cameraCountsBySurface,
                adapterCountsBySurface,
                plasticBoxCountsBySurface,
                totalCameras,
                adapterCameras,
                plasticBoxCameras);
        accumulateNodeMaterials(additionalMaterials, boxNodes);
        accumulateCoefficientMaterials(additionalMaterials,
                materialLengths,
                structureLengthWithoutMaterial,
                applicationSettingsService.getMaterialCoefficients());

        additionalMaterials.values().forEach(acc ->
                addMaterial(overallMaterialTotals, acc.name(), acc.unit(), acc.quantity()));

        List<PrimaryDataSummary.MaterialTotal> materialTotals = buildMaterialTotals(overallMaterialTotals);
        List<PrimaryDataSummary.MaterialTotal> mountingTotals = buildMaterialTotals(mountingElementTotals);

        PrimaryDataSummary.Builder builder = PrimaryDataSummary.builder()
                .withHasData(true)
                .withTotalDeviceCount(totalDevices)
                .withTotalNodes(nodeSummaries.size())
                .withUnnamedConnectionAssignments(unnamedAssignments)
                .withDeclaredConnectionAssignments(declaredAssignments)
                .withTotalCableLength(totalCableLength)
                .withDeviceTypeBreakdown(deviceBreakdown);

        deviceSummaries.forEach(builder::addDeviceTypeSummary);
        cableSummaries.forEach(builder::addCableLengthSummary);
        functionSummaries.forEach(builder::addCableFunctionSummary);
        nodeSummaries.forEach(builder::addNodeSummary);
        cameraDetails.stream()
                .sorted(Comparator
                        .comparing((PrimaryDataSummary.CameraDetail detail) -> detail.getDeviceTypeName(),
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(detail -> detail.getInstallLocation(),
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(detail -> detail.getConnectionPoint(),
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(builder::addCameraDetail);
        additionalMaterials.values().stream()
                .sorted(Comparator.comparing(MaterialAccumulator::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(acc -> new AdditionalMaterialItem(acc.name(), acc.unit(), acc.quantity()))
                .forEach(builder::addAdditionalMaterial);
        materialTotals.forEach(builder::addMaterialTotal);
        mountingTotals.forEach(builder::addMountingElementTotal);
        return builder.build();
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

    private String determineGroupLabel(String label, String fallback) {
        if (StringUtils.hasText(label)) {
            return label.trim();
        }
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }
        return "Без названия";
    }

    private boolean isBoxNode(String elementName) {
        if (!StringUtils.hasText(elementName)) {
            return false;
        }
        String normalized = Normalizer.normalize(elementName, Normalizer.Form.NFKD).toLowerCase();
        return normalized.contains("ящ");
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

    private String combineAmountWithUnit(String rawAmount, String rawUnit) {
        String amount = trimText(rawAmount);
        String unit = trimText(rawUnit);
        if (!StringUtils.hasText(unit)) {
            return amount;
        }
        if (!StringUtils.hasText(amount)) {
            return unit;
        }
        String normalizedAmount = amount.toLowerCase(Locale.ROOT);
        String normalizedUnit = unit.toLowerCase(Locale.ROOT);
        if (normalizedAmount.contains(normalizedUnit)) {
            return amount;
        }
        return amount + " " + unit;
    }

    private String trimText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void accumulateCameraMaterials(Map<String, MaterialAccumulator> accumulator,
                                           EnumMap<SurfaceType, Integer> countsBySurface,
                                           EnumMap<SurfaceType, Integer> adapterCountsBySurface,
                                           EnumMap<SurfaceType, Integer> plasticBoxCountsBySurface,
                                           int totalCameras,
                                           int adapterCameras,
                                           int plasticBoxCameras) {
        if (totalCameras <= 0) {
            return;
        }
        if (plasticBoxCameras > 0) {
            addMaterial(accumulator, "Пластиковая коробка для камеры (опция)", "шт", plasticBoxCameras);
        }
        if (adapterCameras > 0) {
            addMaterial(accumulator, "Адаптер для камеры (опция)", "шт", adapterCameras);
        }
        addMaterial(accumulator, "Разъём RJ-45 (8P8C)", "шт", totalCameras * 2L);
        countsBySurface.forEach((surface, count) -> {
            if (count == null || count <= 0) {
                return;
            }
            SurfaceType effective = surface != null ? surface : SurfaceType.UNKNOWN;
            String fastener = effective.getFastenerName();
            addMaterial(accumulator, "Крепёж для камер — " + fastener, "шт", count * 4L);
        });
        plasticBoxCountsBySurface.forEach((surface, count) -> {
            if (count == null || count <= 0) {
                return;
            }
            SurfaceType effective = surface != null ? surface : SurfaceType.UNKNOWN;
            String fastener = effective.getFastenerName();
            addMaterial(accumulator, "Крепёж для коробок (опция) — " + fastener, "шт", count * 4L);
        });
        adapterCountsBySurface.forEach((surface, count) -> {
            if (count == null || count <= 0) {
                return;
            }
            SurfaceType effective = surface != null ? surface : SurfaceType.UNKNOWN;
            String fastener = effective.getFastenerName();
            addMaterial(accumulator, "Крепёж для адаптеров (опция) — " + fastener, "шт", count * 4L);
        });
    }

    private void accumulateNodeMaterials(Map<String, MaterialAccumulator> accumulator, int boxNodes) {
        if (boxNodes <= 0) {
            return;
        }
        addMaterial(accumulator, "Двухместная розетка", "шт", boxNodes);
        addMaterial(accumulator, "Бокс для автоматов", "шт", boxNodes);
        addMaterial(accumulator, "Автоматический выключатель 6А", "шт", boxNodes * 2L);
    }

    private void accumulateCoefficientMaterials(Map<String, MaterialAccumulator> accumulator,
                                                MaterialLengthStats materialLengths,
                                                double structureLengthWithoutMaterial,
                                                ApplicationSettingsService.MaterialCoefficients coefficients) {
        double clipsPerMeter = Math.max(coefficients.clipsPerMeter(), 0.0);
        double tiesPerMeter = Math.max(coefficients.tiesPerMeter(), 0.0);

        if (clipsPerMeter > 0) {
            double corrugatedLength = materialLengths.corrugatedLength();
            if (corrugatedLength > 0) {
                addMaterial(accumulator, "Клипсы для гофры", "шт", Math.ceil(corrugatedLength * clipsPerMeter));
            }
        }

        double tiesBaseLength = Math.max(structureLengthWithoutMaterial, 0.0);
        if (tiesPerMeter > 0 && tiesBaseLength > 0) {
            addMaterial(accumulator, "Нейлоновые стяжки (по кабелю)", "шт",
                    Math.ceil(tiesBaseLength * tiesPerMeter));
        }
    }

    private double parseLength(String value) {
        if (!StringUtils.hasText(value)) {
            return 0.0;
        }
        String normalized = value.trim().replace(',', '.');
        StringBuilder builder = new StringBuilder();
        for (char ch : normalized.toCharArray()) {
            if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '-') {
                builder.append(ch);
            } else if (builder.length() > 0) {
                break;
            }
        }
        if (builder.length() == 0) {
            return 0.0;
        }
        try {
            return Double.parseDouble(builder.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private MaterialLengthStats collectMaterialLengths(PrimaryDataSnapshot snapshot) {
        MaterialLengthStats stats = new MaterialLengthStats();
        if (snapshot == null) {
            return stats;
        }
        if (snapshot.getMaterialGroups() != null) {
            for (PrimaryDataSnapshot.MaterialGroup group : snapshot.getMaterialGroups()) {
                if (group == null || group.getMaterials() == null) {
                    continue;
                }
                for (PrimaryDataSnapshot.MaterialUsage usage : group.getMaterials()) {
                    if (usage == null) {
                        continue;
                    }
                    double length = parseLength(usage.getAmount());
                    if (length <= 0) {
                        continue;
                    }
                    if (!isMeterUnit(usage.getUnit()) && !looksLikeMeters(usage.getAmount())) {
                        continue;
                    }
                    stats.addMaterialLength(usage.getMaterialName(), length);
                    stats.addSurfaceLength(usage.getLayingSurfaceCategory(), length);
                }
            }
        }
        if (snapshot.getConnectionPoints() != null) {
            for (PrimaryDataSnapshot.ConnectionPoint point : snapshot.getConnectionPoints()) {
                if (point == null) {
                    continue;
                }
                double length = Math.max(0.0, defaultDouble(point.getDistanceToPower()));
                if (length <= 0) {
                    continue;
                }
                if (!StringUtils.hasText(point.getLayingMaterialName())) {
                    continue;
                }
                stats.addMaterialLength(point.getLayingMaterialName(), length);
                stats.addSurfaceLength(point.getLayingSurfaceCategory(), length);
            }
        }
        if (snapshot.getMountingElements() != null) {
            for (PrimaryDataSnapshot.MountingRequirement requirement : snapshot.getMountingElements()) {
                if (requirement == null || requirement.getMaterials() == null) {
                    continue;
                }
                for (PrimaryDataSnapshot.MountingMaterial material : requirement.getMaterials()) {
                    if (material == null) {
                        continue;
                    }
                    double length = parseLength(material.getAmount());
                    if (length <= 0) {
                        continue;
                    }
                    if (!isMeterUnit(material.getUnit()) && !looksLikeMeters(material.getAmount())) {
                        continue;
                    }
                    stats.addMaterialLength(material.getMaterialName(), length);
                }
            }
        }
        return stats;
    }

    private boolean looksLikeMeters(String amount) {
        if (!StringUtils.hasText(amount)) {
            return false;
        }
        String normalized = amount.toLowerCase();
        return normalized.contains("м");
    }

    private boolean isMeterUnit(String unit) {
        if (!StringUtils.hasText(unit)) {
            return false;
        }
        String normalized = unit.trim().toLowerCase();
        return normalized.contains("м");
    }

    private List<NodeSummary> buildNodeSummaries(List<NodeContext> contexts) {
        List<NodeSummary> summaries = new ArrayList<>();
        for (NodeContext context : contexts) {
            if (context == null) {
                continue;
            }
            List<PrimaryDataSummary.NodeMaterialGroupSummary> groups = new ArrayList<>();
            context.groupMaterials.forEach((label, materials) ->
                    groups.add(new PrimaryDataSummary.NodeMaterialGroupSummary(label, materials)));
            groups.sort(Comparator.comparing(PrimaryDataSummary.NodeMaterialGroupSummary::getLabel,
                    Comparator.nullsLast(String::compareToIgnoreCase)));
            List<PrimaryDataSummary.MaterialTotal> totals = buildMaterialTotals(context.totals);
            PrimaryDataSnapshot.ConnectionPoint point = context.point;
            summaries.add(new NodeSummary(
                    context.displayName,
                    point.getMountingElementName(),
                    point.getDistanceToPower(),
                    context.powerCableName,
                    point.getLayingMaterialName(),
                    point.getLayingMaterialUnit(),
                    point.getLayingSurface(),
                    point.getLayingSurfaceCategory(),
                    groups,
                    totals));
        }
        return summaries;
    }

    private List<PrimaryDataSummary.MaterialTotal> buildMaterialTotals(Map<String, MaterialAccumulator> totals) {
        List<PrimaryDataSummary.MaterialTotal> result = new ArrayList<>();
        totals.values().stream()
                .sorted(Comparator.comparing(MaterialAccumulator::name,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(acc -> result.add(new PrimaryDataSummary.MaterialTotal(acc.name(), acc.unit(), acc.quantity())));
        return result;
    }

    private void addNodeMaterial(NodeContext context,
                                 String groupLabel,
                                 String materialName,
                                 String amountWithUnit,
                                 Double quantity,
                                 String unit,
                                 String surfaceLabel,
                                 Map<String, MaterialAccumulator> overallTotals) {
        if (context == null) {
            return;
        }
        String effectiveLabel = StringUtils.hasText(groupLabel) ? groupLabel.trim() : "Без группы";
        String trimmedName = trimText(materialName);
        String amount = StringUtils.hasText(amountWithUnit)
                ? amountWithUnit
                : (quantity != null ? formatQuantity(quantity, unit) : null);
        List<MaterialUsageSummary> materials = context.groupMaterials
                .computeIfAbsent(effectiveLabel, key -> new ArrayList<>());
        materials.add(new MaterialUsageSummary(trimmedName, amount, surfaceLabel));
        if (quantity != null && quantity > 0 && StringUtils.hasText(trimmedName)) {
            addMaterial(context.totals, trimmedName, unit, quantity);
            addMaterial(overallTotals, trimmedName, unit, quantity);
        }
    }

    private Map<String, String> mapGroupLabelsToNodes(List<PrimaryDataSnapshot.DeviceGroup> groups) {
        Map<String, Set<String>> labelToNodes = new HashMap<>();
        if (groups == null) {
            return Map.of();
        }
        for (PrimaryDataSnapshot.DeviceGroup group : groups) {
            if (group == null) {
                continue;
            }
            String label = normalizeKey(trimText(group.getGroupLabel()));
            String node = normalizeKey(trimText(group.getConnectionPoint()));
            if (!StringUtils.hasText(label) || !StringUtils.hasText(node)) {
                continue;
            }
            labelToNodes.computeIfAbsent(label, key -> new HashSet<>()).add(node);
        }
        Map<String, String> result = new HashMap<>();
        labelToNodes.forEach((label, nodes) -> {
            if (nodes.size() == 1) {
                result.put(label, nodes.iterator().next());
            }
        });
        return result;
    }

    private NodeContext resolveNodeForGroup(String label,
                                            Map<String, String> labelToNodeMap,
                                            Map<String, NodeContext> nodeContextsByNormalizedName) {
        if (nodeContextsByNormalizedName.isEmpty()) {
            return null;
        }
        String normalizedLabel = normalizeKey(label);
        if (!StringUtils.hasText(normalizedLabel)) {
            return null;
        }
        String mappedNode = labelToNodeMap.get(normalizedLabel);
        if (mappedNode != null) {
            NodeContext mapped = nodeContextsByNormalizedName.get(mappedNode);
            if (mapped != null) {
                return mapped;
            }
        }
        return nodeContextsByNormalizedName.get(normalizedLabel);
    }

    private String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return Normalizer.normalize(trimmed, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
    }

    private String resolveUnit(String explicitUnit, String amount) {
        String candidate = trimText(explicitUnit);
        if (StringUtils.hasText(candidate)) {
            return candidate;
        }
        return extractUnitFromAmount(amount);
    }

    private String extractUnitFromAmount(String amount) {
        if (!StringUtils.hasText(amount)) {
            return null;
        }
        String normalized = amount.trim();
        StringBuilder builder = new StringBuilder();
        boolean numberEnded = false;
        for (char ch : normalized.toCharArray()) {
            if (!numberEnded) {
                if ((ch >= '0' && ch <= '9') || ch == '.' || ch == ',' || ch == '-' || Character.isWhitespace(ch)) {
                    continue;
                }
                numberEnded = true;
            }
            if (numberEnded) {
                builder.append(ch);
            }
        }
        String unit = builder.toString().trim();
        return StringUtils.hasText(unit) ? unit : null;
    }

    private String formatQuantity(double value, String unit) {
        double rounded = Math.rint(value);
        String number;
        if (Math.abs(value - rounded) < 1e-3) {
            number = String.format(Locale.getDefault(), "%.0f", rounded);
        } else {
            number = String.format(Locale.getDefault(), "%.2f", value);
        }
        if (StringUtils.hasText(unit)) {
            return number + " " + unit;
        }
        return number;
    }

    private boolean containsIgnoreCase(String source, String needle) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(needle)) {
            return false;
        }
        String normalizedSource = Normalizer.normalize(source, Normalizer.Form.NFKD).toLowerCase();
        String normalizedNeedle = Normalizer.normalize(needle, Normalizer.Form.NFKD).toLowerCase();
        return normalizedSource.contains(normalizedNeedle);
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

    private static class NodeContext {
        private final PrimaryDataSnapshot.ConnectionPoint point;
        private final String displayName;
        private final String normalizedName;
        private final String powerCableName;
        private final Map<String, List<MaterialUsageSummary>> groupMaterials = new LinkedHashMap<>();
        private final Map<String, MaterialAccumulator> totals = new LinkedHashMap<>();

        NodeContext(PrimaryDataSnapshot.ConnectionPoint point,
                    String displayName,
                    String normalizedName,
                    String powerCableName) {
            this.point = point;
            this.displayName = displayName;
            this.normalizedName = normalizedName;
            this.powerCableName = powerCableName;
        }
    }

    private static class MaterialAccumulator {
        private final String name;
        private final String unit;
        private double quantity;

        MaterialAccumulator(String name, String unit, double quantity) {
            this.name = trim(name);
            this.unit = trim(unit);
            this.quantity = quantity;
        }

        void add(double value) {
            this.quantity += value;
        }

        String name() {
            return name;
        }

        String unit() {
            return unit;
        }

        double quantity() {
            return quantity;
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }
    }

    private static class MaterialLengthStats {
        private double total;
        private double corrugated;

        void addMaterialLength(String materialName, double length) {
            if (length <= 0) {
                return;
            }
            total += length;
            if (containsCorrugated(materialName)) {
                corrugated += length;
            }
        }

        void addSurfaceLength(String surfaceCategory, double length) {
            // reserved for future use (e.g., breakdown per surface)
        }

        double totalMaterialLength() {
            return total;
        }

        double corrugatedLength() {
            return corrugated;
        }

        private boolean containsCorrugated(String name) {
            if (!StringUtils.hasText(name)) {
                return false;
            }
            String normalized = Normalizer.normalize(name, Normalizer.Form.NFKD).toLowerCase();
            return normalized.contains("гофр");
        }
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

    private record CableTypeData(String name, CableFunction function) {
    }

    private static class CableLengthAccumulator {
        private final String name;
        private double totalLength;
        private boolean classificationMissing;
        private final CableFunction function;

        CableLengthAccumulator(String name, double totalLength, boolean classificationMissing, CableFunction function) {
            this.name = name;
            this.totalLength = totalLength;
            this.classificationMissing = classificationMissing;
            this.function = function;
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

        CableFunction function() {
            return function;
        }
    }
}
