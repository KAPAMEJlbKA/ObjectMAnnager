package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.service.PrimaryDataDeviceSummaryService.DeviceSummaryResult;
import com.kapamejlbka.objectmanager.service.PrimaryDataMaterialSummaryService.MaterialSummaryResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrimaryDataSummaryService {

    private final PrimaryDataParser parser;
    private final PrimaryDataDeviceSummaryService deviceSummaryService;
    private final PrimaryDataCableSummaryService cableSummaryService;
    private final PrimaryDataMaterialSummaryService materialSummaryService;
    private final PrimaryDataReportBuilder reportBuilder;

    public PrimaryDataSummaryService(PrimaryDataParser parser,
                                     PrimaryDataDeviceSummaryService deviceSummaryService,
                                     PrimaryDataCableSummaryService cableSummaryService,
                                     PrimaryDataMaterialSummaryService materialSummaryService,
                                     PrimaryDataReportBuilder reportBuilder) {
        this.parser = parser;
        this.deviceSummaryService = deviceSummaryService;
        this.cableSummaryService = cableSummaryService;
        this.materialSummaryService = materialSummaryService;
        this.reportBuilder = reportBuilder;
    }

    public PrimaryDataSummary buildSummary(String json) {
        if (!StringUtils.hasText(json)) {
            return PrimaryDataSummary.empty();
        }
        PrimaryDataParser.SchemaVersion version = parser.detectVersion(json);
        PrimaryDataParser.ParsingResult parsingResult = parser.parse(json, version);
        if (parsingResult.hasError()) {
            return PrimaryDataSummary.parseError(parsingResult.errorMessage());
        }
        if (!parsingResult.hasSnapshot()) {
            return PrimaryDataSummary.empty();
        }
        PrimaryDataSnapshot snapshot = parsingResult.snapshot();
        DeviceSummaryResult deviceSummary = deviceSummaryService.summarize(snapshot);
        PrimaryDataCableSummaryService.CableSummaryResult cableSummary = cableSummaryService.summarize(snapshot);
        MaterialSummaryResult materialSummary = materialSummaryService.summarize(snapshot, deviceSummary, cableSummary);
        return reportBuilder.build(snapshot, deviceSummary, cableSummary, materialSummary);
    }
}
