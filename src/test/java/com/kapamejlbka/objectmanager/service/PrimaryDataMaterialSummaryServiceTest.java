package com.kapamejlbka.objectmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.ConnectionPoint;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialGroup;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MaterialUsage;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MountingMaterial;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot.MountingRequirement;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.SurfaceType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrimaryDataMaterialSummaryServiceTest {

    private static final PrimaryDataCableSummaryService.CableSummaryResult EMPTY_CABLE_SUMMARY =
            new PrimaryDataCableSummaryService.CableSummaryResult(
                    new LinkedHashMap<>(),
                    new EnumMap<>(CableFunction.class),
                    0.0,
                    0.0,
                    new HashSet<>());

    @Test
    void corrugatedRouteAddsClipsAndDowels() {
        PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
        MaterialUsage corrugated = new MaterialUsage();
        corrugated.setMaterialName("Гофрированная труба");
        corrugated.setAmount("1");
        corrugated.setUnit("м");
        MaterialGroup group = new MaterialGroup();
        group.setGroupName("Трасса гофры");
        group.setMaterials(List.of(corrugated));
        snapshot.setMaterialGroups(List.of(group));

        MountingMaterial dowel = new MountingMaterial();
        dowel.setMaterialName("Дюбель-гвоздь");
        dowel.setUnit("шт");
        dowel.setAmount("4");
        MountingRequirement requirement = new MountingRequirement();
        requirement.setElementName("Крепление трассы");
        requirement.setQuantity("1");
        requirement.setMaterials(List.of(dowel));
        snapshot.setMountingElements(List.of(requirement));

        ApplicationSettingsService settings = mock(ApplicationSettingsService.class);
        when(settings.getMaterialCoefficients()).thenReturn(new ApplicationSettingsService.MaterialCoefficients(2.0, 0.0));
        PrimaryDataMaterialSummaryService service = new PrimaryDataMaterialSummaryService(settings);

        PrimaryDataMaterialSummaryService.MaterialSummaryResult result = service.summarize(
                snapshot,
                PrimaryDataDeviceSummaryService.DeviceSummaryResult.empty(),
                EMPTY_CABLE_SUMMARY);

        assertThat(quantityByName(result.materialTotals(), "Клипсы для гофры")).isEqualTo(2.0);
        assertThat(quantityByName(result.materialTotals(), "Дюбель-гвоздь")).isEqualTo(4.0);
    }

    @Test
    void singleCameraAddsFastenersAndConnectors() {
        EnumMap<SurfaceType, Integer> cameraCounts = new EnumMap<>(SurfaceType.class);
        cameraCounts.put(SurfaceType.WALL, 1);
        PrimaryDataDeviceSummaryService.DeviceSummaryResult deviceSummary = new PrimaryDataDeviceSummaryService.DeviceSummaryResult(
                new LinkedHashMap<>(),
                1,
                0,
                new HashSet<>(),
                new HashMap<>(),
                cameraCounts,
                new EnumMap<>(SurfaceType.class),
                new EnumMap<>(SurfaceType.class),
                1,
                0,
                0,
                new ArrayList<>());

        ApplicationSettingsService settings = mock(ApplicationSettingsService.class);
        when(settings.getMaterialCoefficients()).thenReturn(new ApplicationSettingsService.MaterialCoefficients(0.0, 0.0));

        PrimaryDataMaterialSummaryService service = new PrimaryDataMaterialSummaryService(settings);
        PrimaryDataMaterialSummaryService.MaterialSummaryResult result = service.summarize(
                new PrimaryDataSnapshot(),
                deviceSummary,
                EMPTY_CABLE_SUMMARY);

        assertThat(quantityByName(result.materialTotals(), "Разъём RJ-45 (8P8C)")).isEqualTo(2.0);
        assertThat(quantityByName(result.materialTotals(), "Крепёж для камер — Дюбель-гвоздь")).isEqualTo(4.0);
    }

    @Test
    void cabinetAutoCalculatesFerrules() {
        ConnectionPoint cabinet = new ConnectionPoint();
        cabinet.setName("Шкаф");
        cabinet.setBreakerCount(1);
        PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
        snapshot.setConnectionPoints(List.of(cabinet));

        ApplicationSettingsService settings = mock(ApplicationSettingsService.class);
        when(settings.getMaterialCoefficients()).thenReturn(new ApplicationSettingsService.MaterialCoefficients(0.0, 0.0));
        PrimaryDataMaterialSummaryService service = new PrimaryDataMaterialSummaryService(settings);

        PrimaryDataMaterialSummaryService.MaterialSummaryResult result = service.summarize(
                snapshot,
                PrimaryDataDeviceSummaryService.DeviceSummaryResult.empty(),
                EMPTY_CABLE_SUMMARY);

        assertThat(quantityByName(result.materialTotals(), "Наконечники НШВИ")).isEqualTo(4.0);
        assertThat(quantityByName(result.materialTotals(), "Автоматический выключатель 6А")).isEqualTo(2.0);
        assertThat(quantityByName(result.materialTotals(), "Бокс для автоматов")).isEqualTo(1.0);
    }

    @Test
    void opticalLineSummarizesLengthAndSplices() {
        MaterialUsage fiber = new MaterialUsage();
        fiber.setMaterialName("Оптическая линия");
        fiber.setAmount("120");
        fiber.setUnit("м");

        MaterialUsage splices = new MaterialUsage();
        splices.setMaterialName("Сварка оптического волокна");
        splices.setAmount("2");
        splices.setUnit("шт");

        MaterialGroup group = new MaterialGroup();
        group.setGroupName("Оптика");
        group.setMaterials(List.of(fiber, splices));

        PrimaryDataSnapshot snapshot = new PrimaryDataSnapshot();
        snapshot.setMaterialGroups(List.of(group));

        ApplicationSettingsService settings = mock(ApplicationSettingsService.class);
        when(settings.getMaterialCoefficients()).thenReturn(new ApplicationSettingsService.MaterialCoefficients(0.0, 0.0));
        PrimaryDataMaterialSummaryService service = new PrimaryDataMaterialSummaryService(settings);

        PrimaryDataMaterialSummaryService.MaterialSummaryResult result = service.summarize(
                snapshot,
                PrimaryDataDeviceSummaryService.DeviceSummaryResult.empty(),
                EMPTY_CABLE_SUMMARY);

        assertThat(quantityByName(result.materialTotals(), "Оптическая линия")).isEqualTo(120.0);
        assertThat(quantityByName(result.materialTotals(), "Сварка оптического волокна")).isEqualTo(2.0);
    }

    private double quantityByName(List<PrimaryDataSummary.MaterialTotal> totals, String name) {
        return totals.stream()
                .filter(total -> name.equals(total.getName()))
                .mapToDouble(PrimaryDataSummary.MaterialTotal::getQuantity)
                .findFirst()
                .orElse(0.0);
    }
}
