package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.CableFunction;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.CableFunctionSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.CableLengthSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.MaterialGroupSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.NodeSummary;
import com.kapamejlbka.objectmanager.service.PrimaryDataDeviceSummaryService.DeviceSummaryResult;
import com.kapamejlbka.objectmanager.service.PrimaryDataMaterialSummaryService.MaterialSummaryResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PrimaryDataReportBuilder {

    public PrimaryDataSummary build(PrimaryDataSnapshot snapshot,
                                    DeviceSummaryResult deviceSummary,
                                    PrimaryDataCableSummaryService.CableSummaryResult cableSummary,
                                    MaterialSummaryResult materialSummary) {
        PrimaryDataSummary.Builder builder = PrimaryDataSummary.builder()
                .withHasData(true)
                .withTotalDeviceCount(resolveTotalDevices(snapshot, deviceSummary))
                .withTotalNodes(resolveTotalNodes(snapshot, materialSummary.nodeSummaries()))
                .withUnnamedConnectionAssignments(deviceSummary.unnamedAssignments())
                .withDeclaredConnectionAssignments(resolveDeclaredAssignments(snapshot,
                        cableSummary.definedConnectionPoints(),
                        deviceSummary.assignedConnectionPoints()))
                .withTotalCableLength(cableSummary.totalCableLength())
                .withDeviceTypeBreakdown(buildDeviceBreakdown(deviceSummary.deviceTypeCounts()));

        buildDeviceSummaries(deviceSummary.deviceTypeCounts()).forEach(builder::addDeviceTypeSummary);
        buildCableSummaries(cableSummary.cableLengthMap()).forEach(builder::addCableLengthSummary);
        buildFunctionSummaries(cableSummary.functionTotals()).forEach(builder::addCableFunctionSummary);
        materialSummary.nodeSummaries().forEach(builder::addNodeSummary);

        deviceSummary.cameraDetails().stream()
                .sorted(Comparator
                        .comparing(PrimaryDataSummary.CameraDetail::getDeviceTypeName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(PrimaryDataSummary.CameraDetail::getInstallLocation, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(PrimaryDataSummary.CameraDetail::getConnectionPoint, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(builder::addCameraDetail);

        buildAdditionalMaterials(materialSummary.additionalMaterials()).forEach(builder::addAdditionalMaterial);
        materialSummary.materialTotals().forEach(builder::addMaterialTotal);
        materialSummary.mountingElementTotals().forEach(builder::addMountingElementTotal);
        materialSummary.materialGroupSummaries().forEach(builder::addMaterialGroupSummary);
        builder.withOverallMaterialSummary(materialSummary.overallMaterialSummary());
        return builder.build();
    }

    private int resolveTotalDevices(PrimaryDataSnapshot snapshot, DeviceSummaryResult deviceSummary) {
        if (snapshot != null && snapshot.getTotalDeviceCount() != null && snapshot.getTotalDeviceCount() > 0) {
            return snapshot.getTotalDeviceCount();
        }
        return deviceSummary.totalDevices();
    }

    private int resolveTotalNodes(PrimaryDataSnapshot snapshot, List<NodeSummary> nodeSummaries) {
        if (snapshot != null && snapshot.getTotalNodeCount() != null && snapshot.getTotalNodeCount() > 0) {
            return snapshot.getTotalNodeCount();
        }
        return nodeSummaries.size();
    }

    private Integer resolveDeclaredAssignments(PrimaryDataSnapshot snapshot,
                                               java.util.Set<String> definedConnectionPoints,
                                               java.util.Set<String> assignedConnectionPoints) {
        int recordedConnectionPoints = 0;
        if (snapshot != null) {
            recordedConnectionPoints = Math.max(0, snapshot.getTotalConnectionPoints());
        }
        if (recordedConnectionPoints == 0 && !definedConnectionPoints.isEmpty()) {
            recordedConnectionPoints = definedConnectionPoints.size();
        }
        if (recordedConnectionPoints == 0 && !assignedConnectionPoints.isEmpty()) {
            recordedConnectionPoints = assignedConnectionPoints.size();
        }
        return recordedConnectionPoints > 0 ? recordedConnectionPoints : null;
    }

    private List<DeviceTypeSummary> buildDeviceSummaries(Map<String, Integer> deviceTypeCounts) {
        List<DeviceTypeSummary> summaries = new ArrayList<>();
        deviceTypeCounts.forEach((name, quantity) ->
                summaries.add(new DeviceTypeSummary(name, quantity != null ? quantity : 0)));
        summaries.sort(Comparator.comparing(DeviceTypeSummary::getDeviceTypeName,
                Comparator.nullsLast(String::compareToIgnoreCase)));
        return summaries;
    }

    private String buildDeviceBreakdown(Map<String, Integer> deviceTypeCounts) {
        return deviceTypeCounts.entrySet().stream()
                .map(entry -> String.format(Locale.getDefault(), "%s — %d",
                        entry.getKey(), entry.getValue() != null ? entry.getValue() : 0))
                .collect(Collectors.joining(", "));
    }

    private List<CableLengthSummary> buildCableSummaries(Map<String, CableLengthAccumulator> cableLengthMap) {
        List<CableLengthSummary> summaries = new ArrayList<>();
        cableLengthMap.values().stream()
                .sorted(Comparator.comparing(CableLengthAccumulator::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(acc -> summaries.add(new CableLengthSummary(
                        acc.name(),
                        acc.totalLength(),
                        acc.classificationMissing())));
        return summaries;
    }

    private List<CableFunctionSummary> buildFunctionSummaries(EnumMap<CableFunction, Double> functionTotals) {
        List<CableFunctionSummary> summaries = new ArrayList<>();
        functionTotals.forEach((function, total) -> {
            if (total != null && total > 0) {
                summaries.add(new CableFunctionSummary(function.getDisplayName(), total));
            }
        });
        summaries.sort(Comparator.comparing(CableFunctionSummary::getFunctionName));
        return summaries;
    }

    private List<AdditionalMaterialItem> buildAdditionalMaterials(Map<String, MaterialAccumulator> materials) {
        return materials.values().stream()
                .sorted(Comparator.comparing(MaterialAccumulator::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(acc -> new AdditionalMaterialItem(acc.name(), acc.unit(), acc.quantity()))
                .collect(Collectors.toList());
    }
}
