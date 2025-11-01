package com.kapamejlbka.objectmanager.web;

import com.kapamejlbka.objectmanager.model.MapProvider;
import com.kapamejlbka.objectmanager.model.ManagedObject;
import com.kapamejlbka.objectmanager.model.ObjectChange;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.repository.ObjectChangeRepository;
import com.kapamejlbka.objectmanager.service.ApplicationSettingsService;
import com.kapamejlbka.objectmanager.service.ManagedObjectService;
import com.kapamejlbka.objectmanager.service.PdfReportService;
import com.kapamejlbka.objectmanager.service.PrimaryDataSummaryService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ObjectViewController extends ObjectController {

    private final ManagedObjectService managedObjectService;
    private final ObjectChangeRepository objectChangeRepository;
    private final ApplicationSettingsService applicationSettingsService;
    private final PrimaryDataSummaryService primaryDataSummaryService;
    private final PdfReportService pdfReportService;

    public ObjectViewController(ManagedObjectService managedObjectService,
                                ObjectChangeRepository objectChangeRepository,
                                ApplicationSettingsService applicationSettingsService,
                                PrimaryDataSummaryService primaryDataSummaryService,
                                PdfReportService pdfReportService) {
        this.managedObjectService = managedObjectService;
        this.objectChangeRepository = objectChangeRepository;
        this.applicationSettingsService = applicationSettingsService;
        this.primaryDataSummaryService = primaryDataSummaryService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("objects", managedObjectService.listVisibleObjects());
        return "objects/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        ManagedObject managedObject = managedObjectService.getById(id);
        List<ObjectChange> history = objectChangeRepository.findAllByManagedObjectOrderByChangedAtDesc(managedObject);
        MapProvider mapProvider = applicationSettingsService.getMapProvider();
        model.addAttribute("object", managedObject);
        model.addAttribute("history", history);
        model.addAttribute("mapProvider", mapProvider);
        model.addAttribute("coordinateDisplay", formatCoordinates(managedObject.getLatitude(), managedObject.getLongitude()));
        model.addAttribute("mapLink", buildMapLink(mapProvider, managedObject.getLatitude(), managedObject.getLongitude()));
        PrimaryDataSummary primarySummary = primaryDataSummaryService.buildSummary(managedObject.getPrimaryData());
        model.addAttribute("primarySummary", primarySummary);
        return "objects/detail";
    }

    @GetMapping("/{id}/report.pdf")
    public ResponseEntity<byte[]> downloadReport(@PathVariable UUID id) {
        ManagedObject managedObject = managedObjectService.getById(id);
        byte[] pdf = pdfReportService.buildObjectReport(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(buildReportFileName(managedObject), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }

    private String buildReportFileName(ManagedObject managedObject) {
        String objectBase = normalizeFileToken(managedObject != null ? managedObject.getName() : null, "object");
        String customerBase = normalizeFileToken(managedObject != null && managedObject.getCustomer() != null
                ? managedObject.getCustomer().getName() : null, null);
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(customerBase)) {
            builder.append(customerBase).append('-');
        }
        builder.append(objectBase).append("-report.pdf");
        return builder.toString();
    }

    private String normalizeFileToken(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback != null ? fallback : null;
        }
        String normalized = value.trim().replaceAll("[^a-zA-Z0-9._-]+", "-");
        if (!StringUtils.hasText(normalized)) {
            return fallback;
        }
        return normalized;
    }

    private String formatCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    private String buildMapLink(MapProvider provider, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        if (provider == MapProvider.GOOGLE) {
            return String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude);
        }
        return String.format("https://yandex.by/maps/?ll=%f,%f&z=16&pt=%f,%f", longitude, latitude, longitude, latitude);
    }
}
