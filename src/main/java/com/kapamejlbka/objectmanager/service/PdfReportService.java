package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.ManagedObject;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.domain.calculation.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmanager.service.PrimaryDataParser.ParsingResult;
import com.kapamejlbka.objectmanager.service.PrimaryDataParser.SchemaVersion;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {

    private final ManagedObjectService managedObjectService;
    private final PrimaryDataSummaryService primaryDataSummaryService;
    private final PrimaryDataParser primaryDataParser;
    private final PdfFontProvider pdfFontProvider;
    private final PdfSectionBuilder sectionBuilder;

    public PdfReportService(ManagedObjectService managedObjectService,
                            PrimaryDataSummaryService primaryDataSummaryService,
                            PrimaryDataParser primaryDataParser,
                            PdfFontProvider pdfFontProvider,
                            PdfSectionBuilder sectionBuilder) {
        this.managedObjectService = managedObjectService;
        this.primaryDataSummaryService = primaryDataSummaryService;
        this.primaryDataParser = primaryDataParser;
        this.pdfFontProvider = pdfFontProvider;
        this.sectionBuilder = sectionBuilder;
    }

    public byte[] buildObjectReport(UUID objectId) {
        ManagedObject object = managedObjectService.getById(objectId);
        PrimaryDataSummary summary = primaryDataSummaryService.buildSummary(object.getPrimaryData());
        PrimaryDataSnapshot snapshot = parseSnapshot(object.getPrimaryData());
        List<AdditionalMaterialItem> additionalMaterials = summary != null ? summary.getAdditionalMaterials() : List.of();
        try (PDDocument document = new PDDocument()) {
            PDFont font = pdfFontProvider.loadFont(document);
            sectionBuilder.buildSections(document, font, object, snapshot, summary, additionalMaterials);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сформировать PDF-отчёт", e);
        }
    }

    private PrimaryDataSnapshot parseSnapshot(String json) {
        if (json == null) {
            return new PrimaryDataSnapshot();
        }
        SchemaVersion version = primaryDataParser.detectVersion(json);
        ParsingResult result = primaryDataParser.parse(json, version);
        if (result.hasSnapshot()) {
            return result.snapshot();
        }
        return new PrimaryDataSnapshot();
    }
}
