package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.ConnectionPoint;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialGroup;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialUsage;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MountingMaterial;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MountingRequirement;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary.MaterialGroupSummary;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary.MaterialUsageSummary;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary.NodeSummary;
import com.kapamejlbka.objectmanager.domain.device.SurfaceType;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrimaryDataMaterialSummaryService {

    private final ApplicationSettingsService applicationSettingsService;

    public PrimaryDataMaterialSummaryService(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService;
    }

    public MaterialSummaryResult summarize(PrimaryDataSnapshot snapshot,
                                           PrimaryDataDeviceSummaryService.DeviceSummaryResult deviceSummary,
                                           PrimaryDataCableSummaryService.CableSummaryResult cableSummary) {
        Map<String, MaterialAccumulator> additionalMaterials = new LinkedHashMap<>();
        additionalMaterials.putAll(deviceSummary.additionalMaterials());
        Map<String, MaterialAccumulator> overallMaterialTotals = new LinkedHashMap<>();
        Map<String, MaterialAccumulator> mountingElementTotals = new LinkedHashMap<>();
        List<NodeContext> nodeContexts = new ArrayList<>();
        Map<String, NodeContext> nodeContextsByNormalizedName = new LinkedHashMap<>();
        List<MaterialGroupSummary> materialGroupSummaries = new ArrayList<>();

        if (snapshot != null) {
            processConnectionPoints(snapshot, nodeContexts, nodeContextsByNormalizedName,
                    overallMaterialTotals);
            processMaterialGroups(snapshot, nodeContextsByNormalizedName, materialGroupSummaries,
                    overallMaterialTotals);
            processMountingRequirements(snapshot, mountingElementTotals, overallMaterialTotals);
        }

        List<NodeSummary> nodeSummaries = buildNodeSummaries(nodeContexts);
        MaterialLengthStats materialLengths = collectMaterialLengths(snapshot);
        accumulateCameraMaterials(additionalMaterials,
                deviceSummary.cameraCountsBySurface(),
                deviceSummary.adapterCountsBySurface(),
                deviceSummary.plasticBoxCountsBySurface(),
                deviceSummary.totalCameras(),
                deviceSummary.adapterCameras(),
                deviceSummary.plasticBoxCameras());
        accumulateCoefficientMaterials(additionalMaterials,
                materialLengths,
                cableSummary.structureLengthWithoutMaterial(),
                applicationSettingsService.getMaterialCoefficients());

        additionalMaterials.values().forEach(acc ->
                addMaterial(overallMaterialTotals, acc.name(), acc.unit(), acc.quantity()));

        List<PrimaryDataSummary.MaterialTotal> materialTotals = buildMaterialTotals(overallMaterialTotals);
        List<PrimaryDataSummary.MaterialTotal> mountingTotals = buildMaterialTotals(mountingElementTotals);
        String overallMaterialSummaryText = buildOverallMaterialSummary(materialTotals);

        return new MaterialSummaryResult(nodeSummaries,
                materialGroupSummaries,
                additionalMaterials,
                materialTotals,
                mountingTotals,
                overallMaterialSummaryText);
    }

    private void processConnectionPoints(PrimaryDataSnapshot snapshot,
                                         List<NodeContext> nodeContexts,
                                         Map<String, NodeContext> nodeContextsByNormalizedName,
                                         Map<String, MaterialAccumulator> overallMaterialTotals) {
        int unnamedNodeIndex = 1;
        if (snapshot.getConnectionPoints() == null) {
            return;
        }
        for (ConnectionPoint point : snapshot.getConnectionPoints()) {
            if (point == null) {
                continue;
            }
            String trimmedName = point.getName() != null ? point.getName().trim() : null;
            String displayName = StringUtils.hasText(trimmedName) ? trimmedName : "Без названия";
            String normalizedName = normalizeKey(trimmedName);
            int singleSockets = Math.max(0, defaultInt(point.getSingleSocketCount()));
            int doubleSockets = Math.max(0, defaultInt(point.getDoubleSocketCount()));
            int breakerCount = adjustBreakerCount(point.getBreakerCount());
            int breakerBoxes = adjustBreakerBoxCount(point.getBreakerBoxCount(), breakerCount);
            int nshviCount = Math.max(0, defaultInt(point.getNshviCount()));
            if (nshviCount <= 0) {
                nshviCount = (singleSockets + doubleSockets) * 4 + breakerCount * 2;
            }
            String powerCableName = trimText(point.getPowerCableTypeName());
            NodeContext context = new NodeContext(point,
                    displayName,
                    normalizedName,
                    powerCableName,
                    singleSockets,
                    doubleSockets,
                    breakerCount,
                    breakerBoxes,
                    nshviCount);
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

            if (singleSockets > 0) {
                addMaterial(context.totals, "Одноместная розетка", "шт", singleSockets);
                addMaterial(overallMaterialTotals, "Одноместная розетка", "шт", singleSockets);
            }
            if (doubleSockets > 0) {
                addMaterial(context.totals, "Двухместная розетка", "шт", doubleSockets);
                addMaterial(overallMaterialTotals, "Двухместная розетка", "шт", doubleSockets);
            }
            if (breakerCount > 0) {
                addMaterial(context.totals, "Автоматический выключатель 6А", "шт", breakerCount);
                addMaterial(overallMaterialTotals, "Автоматический выключатель 6А", "шт", breakerCount);
            }
            if (breakerBoxes > 0) {
                addMaterial(context.totals, "Бокс для автоматов", "шт", breakerBoxes);
                addMaterial(overallMaterialTotals, "Бокс для автоматов", "шт", breakerBoxes);
            }
            if (nshviCount > 0) {
                addMaterial(context.totals, "Наконечники НШВИ", "шт", nshviCount);
                addMaterial(overallMaterialTotals, "Наконечники НШВИ", "шт", nshviCount);
            }

            double powerLength = Math.max(0.0, defaultDouble(point.getDistanceToPower()));
            if (powerLength > 0) {
                String unit = trimText(point.getLayingMaterialUnit());
                if (!StringUtils.hasText(unit)) {
                    unit = "м";
                }
                String surfaceLabel = resolveSurfaceLabel(
                        SurfaceType.resolve(point.getLayingSurfaceCategory()).orElse(null),
                        point.getLayingSurface());
                String materialName = trimText(point.getLayingMaterialName());
                String amountWithUnit = formatQuantity(powerLength, unit);
                MaterialUsageSummary material =
                        new MaterialUsageSummary(materialName, amountWithUnit, surfaceLabel);
                addNodeMaterial(context,
                        "Прокладка питания",
                        material,
                        powerLength,
                        unit,
                        overallMaterialTotals);
            }
        }
    }
    private void processMaterialGroups(PrimaryDataSnapshot snapshot,
                                       Map<String, NodeContext> nodeContextsByNormalizedName,
                                       List<MaterialGroupSummary> materialGroupSummaries,
                                       Map<String, MaterialAccumulator> overallMaterialTotals) {
        Map<String, String> labelToNodeMap = mapGroupLabelsToNodes(snapshot.getDeviceGroups());
        if (snapshot.getMaterialGroups() == null) {
            return;
        }
        for (MaterialGroup group : snapshot.getMaterialGroups()) {
            if (group == null || group.getMaterials() == null) {
                continue;
            }
            String label = determineGroupLabel(group.getGroupLabel(), group.getGroupName());
            String groupSurfaceLabel = resolveSurfaceLabel(
                    SurfaceType.resolve(group.getSurfaceCategory()).orElse(null),
                    group.getSurface());
            List<MaterialUsageSummary> groupMaterials = new ArrayList<>();
            NodeContext target = resolveNodeForGroup(label, labelToNodeMap, nodeContextsByNormalizedName);
            for (MaterialUsage usage : group.getMaterials()) {
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
                MaterialUsageSummary material =
                        new MaterialUsageSummary(name, amountWithUnit, surfaceLabel);
                groupMaterials.add(material);
                if (target != null) {
                    addNodeMaterial(target,
                            label,
                            material,
                            quantity,
                            unit,
                            overallMaterialTotals);
                } else if (quantity != null && StringUtils.hasText(name)) {
                    addMaterial(overallMaterialTotals, name, unit, quantity);
                }
            }
            materialGroupSummaries.add(new MaterialGroupSummary(label, groupSurfaceLabel, groupMaterials));
        }
    }

    private void processMountingRequirements(PrimaryDataSnapshot snapshot,
                                             Map<String, MaterialAccumulator> mountingElementTotals,
                                             Map<String, MaterialAccumulator> overallMaterialTotals) {
        if (snapshot.getMountingElements() == null) {
            return;
        }
        for (MountingRequirement requirement : snapshot.getMountingElements()) {
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
                for (MountingMaterial material : requirement.getMaterials()) {
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

    private List<NodeSummary> buildNodeSummaries(List<NodeContext> nodeContexts) {
        List<NodeSummary> summaries = new ArrayList<>();
        for (NodeContext context : nodeContexts) {
            if (context == null) {
                continue;
            }
            PrimaryDataSnapshot.ConnectionPoint point = context.point;
            List<PrimaryDataSummary.NodeMaterialGroupSummary> groups = new ArrayList<>();
            context.groupMaterials.forEach((label, materials) -> {
                if (materials == null) {
                    return;
                }
                groups.add(new PrimaryDataSummary.NodeMaterialGroupSummary(label, materials));
            });
            List<PrimaryDataSummary.MaterialTotal> totals = new ArrayList<>();
            context.totals.values().stream()
                    .sorted(Comparator.comparing(MaterialAccumulator::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .forEach(acc -> totals.add(new PrimaryDataSummary.MaterialTotal(
                            acc.name(),
                            acc.unit(),
                            acc.quantity())));
            NodeSummary summary = new NodeSummary(
                    context.displayName,
                    point.getMountingElementName(),
                    point.getDistanceToPower(),
                    context.powerCableName,
                    point.getLayingMaterialName(),
                    point.getLayingMaterialUnit(),
                    point.getLayingSurface(),
                    point.getLayingSurfaceCategory(),
                    context.singleSocketCount,
                    context.doubleSocketCount,
                    context.breakerCount,
                    context.breakerBoxCount,
                    context.nshviCount,
                    groups,
                    totals);
            summaries.add(summary);
        }
        summaries.sort(Comparator.comparing(NodeSummary::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return summaries;
    }

    private MaterialLengthStats collectMaterialLengths(PrimaryDataSnapshot snapshot) {
        MaterialLengthStats stats = new MaterialLengthStats();
        if (snapshot == null) {
            return stats;
        }
        if (snapshot.getMaterialGroups() != null) {
            for (MaterialGroup group : snapshot.getMaterialGroups()) {
                if (group == null || group.getMaterials() == null) {
                    continue;
                }
                for (MaterialUsage usage : group.getMaterials()) {
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
            for (ConnectionPoint point : snapshot.getConnectionPoints()) {
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
            for (MountingRequirement requirement : snapshot.getMountingElements()) {
                if (requirement == null || requirement.getMaterials() == null) {
                    continue;
                }
                for (MountingMaterial material : requirement.getMaterials()) {
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

    private void accumulateCameraMaterials(Map<String, MaterialAccumulator> accumulator,
                                           Map<SurfaceType, Integer> countsBySurface,
                                           Map<SurfaceType, Integer> adapterCountsBySurface,
                                           Map<SurfaceType, Integer> plasticBoxCountsBySurface,
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

    private List<PrimaryDataSummary.MaterialTotal> buildMaterialTotals(Map<String, MaterialAccumulator> accumulator) {
        List<PrimaryDataSummary.MaterialTotal> totals = new ArrayList<>();
        accumulator.values().stream()
                .sorted(Comparator.comparing(MaterialAccumulator::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(entry -> totals.add(new PrimaryDataSummary.MaterialTotal(
                        entry.name(),
                        entry.unit(),
                        entry.quantity())));
        return totals;
    }

    private String buildOverallMaterialSummary(List<PrimaryDataSummary.MaterialTotal> totals) {
        if (totals.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (PrimaryDataSummary.MaterialTotal total : totals) {
            if (total == null) {
                continue;
            }
            String name = total.getName();
            String amount = formatQuantity(total.getQuantity(), total.getUnit());
            joiner.add(name + " — " + amount);
        }
        return joiner.toString();
    }

    private void addNodeMaterial(NodeContext context,
                                 String groupLabel,
                                 MaterialUsageSummary material,
                                 Double quantity,
                                 String unit,
                                 Map<String, MaterialAccumulator> overallTotals) {
        String effectiveLabel = StringUtils.hasText(groupLabel) ? groupLabel.trim() : "Без группы";
        List<MaterialUsageSummary> materials = context.groupMaterials
                .computeIfAbsent(effectiveLabel, key -> new ArrayList<>());
        materials.add(material);
        String trimmedName = trimText(material.getMaterialName());
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

    private String determineGroupLabel(String label, String fallback) {
        if (StringUtils.hasText(label)) {
            return label.trim();
        }
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }
        return "Без названия";
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

    private int adjustBreakerCount(Integer breakerCount) {
        int count = Math.max(0, defaultInt(breakerCount));
        if (count > 0 && count < 2) {
            count = 2;
        }
        return count;
    }

    private int adjustBreakerBoxCount(Integer boxCount, int breakerCount) {
        int boxes = Math.max(0, defaultInt(boxCount));
        if (boxes <= 0 && breakerCount > 0) {
            boxes = Math.max(1, (int) Math.ceil(breakerCount / 2.0));
        }
        return boxes;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return Normalizer.normalize(trimmed, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
    }

    public record MaterialSummaryResult(List<NodeSummary> nodeSummaries,
                                        List<MaterialGroupSummary> materialGroupSummaries,
                                        Map<String, MaterialAccumulator> additionalMaterials,
                                        List<PrimaryDataSummary.MaterialTotal> materialTotals,
                                        List<PrimaryDataSummary.MaterialTotal> mountingElementTotals,
                                        String overallMaterialSummary) {
    }

    private static class NodeContext {
        private final ConnectionPoint point;
        private final String displayName;
        private final String normalizedName;
        private final String powerCableName;
        private final int singleSocketCount;
        private final int doubleSocketCount;
        private final int breakerCount;
        private final int breakerBoxCount;
        private final int nshviCount;
        private final Map<String, List<MaterialUsageSummary>> groupMaterials = new LinkedHashMap<>();
        private final Map<String, MaterialAccumulator> totals = new LinkedHashMap<>();

        NodeContext(ConnectionPoint point,
                    String displayName,
                    String normalizedName,
                    String powerCableName,
                    int singleSocketCount,
                    int doubleSocketCount,
                    int breakerCount,
                    int breakerBoxCount,
                    int nshviCount) {
            this.point = point;
            this.displayName = displayName;
            this.normalizedName = normalizedName;
            this.powerCableName = powerCableName;
            this.singleSocketCount = singleSocketCount;
            this.doubleSocketCount = doubleSocketCount;
            this.breakerCount = breakerCount;
            this.breakerBoxCount = breakerBoxCount;
            this.nshviCount = nshviCount;
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

}
