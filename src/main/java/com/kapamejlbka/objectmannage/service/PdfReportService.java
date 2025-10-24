package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.DeviceGroup;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.MaterialGroup;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.MaterialUsage;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.MountingRequirement;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.CableFunctionSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmannage.model.SurfaceType;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PdfReportService {

    private static final String FONT_RESOURCE_PATH = "fonts/NotoSans-Regular.ttf";
    private final Resource fontResource = new ClassPathResource(FONT_RESOURCE_PATH);
    private final ApplicationSettingsService applicationSettingsService;

    public PdfReportService(ObjectProvider<com.fasterxml.jackson.databind.ObjectMapper> ignored,
                            ApplicationSettingsService applicationSettingsService) {
        // ObjectMapper is already provided in ObjectController, no-op constructor keeps parity with other services.
        this.applicationSettingsService = applicationSettingsService;
    }

    public byte[] buildObjectReport(ManagedObject object,
                                    PrimaryDataSnapshot snapshot,
                                    PrimaryDataSummary summary) {
        PrimaryDataSnapshot safeSnapshot = snapshot != null ? snapshot : new PrimaryDataSnapshot();
        try (PDDocument document = new PDDocument()) {
            PDFont font = loadFont(document);
            ApplicationSettingsService.CompanyLogo logo = applicationSettingsService.getCompanyLogo().orElse(null);
            PdfBuilder builder = new PdfBuilder(document, font, logo);
            builder.addTitlePage(object, summary);
            List<AdditionalMaterialItem> additional = summary != null ? summary.getAdditionalMaterials() : List.of();
            builder.addMountingMaterialsSection(safeSnapshot, additional);
            builder.addEquipmentSection(summary);
            builder.addGroupsSection(safeSnapshot);
            builder.finish();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сформировать PDF-отчёт", e);
        }
    }

    private PDFont loadFont(PDDocument document) {
        if (fontResource.exists()) {
            try (InputStream inputStream = fontResource.getInputStream()) {
                return PDType0Font.load(document, inputStream, true);
            } catch (IOException ex) {
                // fall through to default font
            }
        }
        return PDType1Font.HELVETICA;
    }

    private static class PdfBuilder implements Closeable {
        private static final float MARGIN = 54f;
        private static final float LINE_SPACING_RATIO = 1.4f;

        private final PDDocument document;
        private final PDFont font;
        private final PDImageXObject logoImage;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;
        private float pageWidth;
        private float pageHeight;

        PdfBuilder(PDDocument document, PDFont font, ApplicationSettingsService.CompanyLogo logo) throws IOException {
            this.document = document;
            this.font = font;
            this.logoImage = createLogoImage(document, logo);
        }

        void addTitlePage(ManagedObject object, PrimaryDataSummary summary) throws IOException {
            newPage();
            drawLogo();
            writeCentered("Отчёт по объекту", 28f);
            writeCentered("Object Manager", 16f);
            writeSpacing(32f);
            writeParagraph(String.format("Объект: %s", safeText(object.getName())), 14f);
            if (object.getCustomer() != null) {
                writeParagraph(String.format("Заказчик: %s", safeText(object.getCustomer().getName())), 14f);
            }
            if (StringUtils.hasText(object.getDescription())) {
                writeParagraph(String.format("Описание: %s", object.getDescription().trim()), 12f);
            }
            writeParagraph("Дата отчёта: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), 12f);
            if (summary != null && summary.isHasData()) {
                writeSpacing(18f);
                writeParagraph(String.format(Locale.getDefault(),
                        "Всего устройств: %d • Точек подключения: %d",
                        summary.getTotalDeviceCount(),
                        summary.getDeclaredConnectionAssignments() != null
                                ? summary.getDeclaredConnectionAssignments() : 0), 12f);
            }
        }

        void addMountingMaterialsSection(PrimaryDataSnapshot snapshot,
                                         List<AdditionalMaterialItem> calculatedMaterials) throws IOException {
            newPage();
            writeSectionHeading("Перечень монтажных материалов");
            List<MountingRequirement> requirements = snapshot.getMountingElements();
            if (requirements == null || requirements.isEmpty()) {
                writeParagraph("Монтажные элементы не указаны.", 12f);
            } else {
                List<String> items = new ArrayList<>();
                for (MountingRequirement requirement : requirements) {
                    if (requirement == null) {
                        continue;
                    }
                    String element = safeText(requirement.getElementName());
                    String quantity = safeText(requirement.getQuantity());
                    items.add(String.format("%s — %s", element, StringUtils.hasText(quantity) ? quantity : "не указано"));
                }
                writeBulletList(items, 12f);
            }

            writeSpacing(18f);
            writeSubHeading("Материалы по группам");
            List<MaterialGroup> materialGroups = snapshot.getMaterialGroups();
            if (materialGroups == null || materialGroups.isEmpty()) {
                writeParagraph("Материалы по группам не указаны.", 12f);
            } else {
                for (MaterialGroup group : materialGroups) {
                    if (group == null) {
                        continue;
                    }
                    String label = determineGroupLabel(group.getGroupLabel(), group.getGroupName());
                    writeParagraph("Группа: " + label, 12f);
                    List<String> materials = new ArrayList<>();
                    if (group.getMaterials() != null) {
                        for (MaterialUsage usage : group.getMaterials()) {
                            if (usage == null) {
                                continue;
                            }
                            String name = safeText(usage.getMaterialName());
                            String amount = safeText(usage.getAmount());
                            String surface = safeText(usage.getLayingSurface());
                            StringBuilder builder = new StringBuilder(name);
                            if (StringUtils.hasText(amount)) {
                                builder.append(" — ").append(amount.trim());
                            }
                            if (StringUtils.hasText(surface)) {
                                builder.append(" (" + surface.trim() + ")");
                            }
                            materials.add(builder.toString());
                        }
                    }
                    if (materials.isEmpty()) {
                        materials.add("Материалы не указаны.");
                    }
                    writeBulletList(materials, 11f);
                    writeSpacing(12f);
                }
            }

            if (calculatedMaterials != null && !calculatedMaterials.isEmpty()) {
                writeSpacing(18f);
                writeSubHeading("Расчёт дополнительных материалов");
                List<String> calculated = new ArrayList<>();
                for (AdditionalMaterialItem item : calculatedMaterials) {
                    if (item == null) {
                        continue;
                    }
                    String unit = StringUtils.hasText(item.getUnit()) ? item.getUnit().trim() : "шт";
                    calculated.add(String.format(Locale.getDefault(), "%s — %s %s",
                            safeText(item.getName()), formatQuantity(item.getQuantity()), unit));
                }
                if (calculated.isEmpty()) {
                    calculated.add("Дополнительные материалы не определены.");
                }
                writeBulletList(calculated, 11f);
            }
        }

        void addEquipmentSection(PrimaryDataSummary summary) throws IOException {
            newPage();
            writeSectionHeading("Перечень оборудования");
            if (summary == null) {
                writeParagraph("Данные об оборудовании отсутствуют.", 12f);
                return;
            }
            if (summary.isParseError()) {
                writeParagraph("Не удалось прочитать первичные данные: " + safeText(summary.getErrorMessage()), 12f);
                return;
            }
            if (!summary.isHasData()) {
                writeParagraph("Оборудование не указано.", 12f);
            } else {
                writeParagraph(String.format(Locale.getDefault(),
                        "Всего устройств: %d, узлов: %d",
                        summary.getTotalDeviceCount(), summary.getTotalNodes()), 12f);
                if (summary.getDeviceTypeSummaries() != null && !summary.getDeviceTypeSummaries().isEmpty()) {
                    writeSubHeading("Оборудование по типам");
                    List<String> items = new ArrayList<>();
                    for (DeviceTypeSummary typeSummary : summary.getDeviceTypeSummaries()) {
                        items.add(String.format("%s — %d шт.",
                                safeText(typeSummary.getDeviceTypeName()), typeSummary.getQuantity()));
                    }
                    writeBulletList(items, 11f);
                }
                if (summary.getCableFunctionSummaries() != null && !summary.getCableFunctionSummaries().isEmpty()) {
                    writeSubHeading("Длины по категориям кабелей");
                    List<String> cableItems = new ArrayList<>();
                    for (CableFunctionSummary functionSummary : summary.getCableFunctionSummaries()) {
                        cableItems.add(String.format(Locale.getDefault(), "%s — %.2f м",
                                safeText(functionSummary.getFunctionName()), functionSummary.getTotalLength()));
                    }
                    writeBulletList(cableItems, 11f);
                }
            }
        }

        void addGroupsSection(PrimaryDataSnapshot snapshot) throws IOException {
            newPage();
            writeSectionHeading("Группы устройств и материалы");
            Map<String, List<DeviceGroup>> groupsByLabel = groupDevices(snapshot.getDeviceGroups());
            Map<String, MaterialGroup> materialsByLabel = mapMaterials(snapshot.getMaterialGroups());
            Set<String> labels = new LinkedHashSet<>();
            labels.addAll(groupsByLabel.keySet());
            labels.addAll(materialsByLabel.keySet());

            if (labels.isEmpty()) {
                writeParagraph("Группы устройств не заданы.", 12f);
                return;
            }

            for (String rawLabel : labels) {
                String label = determineGroupLabel(rawLabel, rawLabel);
                writeSubHeading("Группа: " + label);

                List<DeviceGroup> deviceGroups = groupsByLabel.getOrDefault(rawLabel, List.of());
                List<String> deviceLines = new ArrayList<>();
                double totalLength = 0;
                for (DeviceGroup group : deviceGroups) {
                    if (group == null) {
                        continue;
                    }
                    int quantity = Math.max(group.getQuantity(), 0);
                    double distance = safeDouble(group.getDistanceToConnectionPoint());
                    totalLength += quantity * distance;
                    StringBuilder builder = new StringBuilder();
                    builder.append(safeText(group.getDeviceTypeName()));
                    builder.append(" — ").append(quantity).append(" шт.");
                    if (StringUtils.hasText(group.getInstallLocation())) {
                        builder.append(", установка: ").append(group.getInstallLocation().trim());
                    }
                    if (StringUtils.hasText(group.getConnectionPoint())) {
                        builder.append(", подключение: ").append(group.getConnectionPoint().trim());
                    }
                    String surfaceLabel = resolveSurfaceLabel(group.getInstallSurfaceCategory());
                    if (surfaceLabel != null) {
                        builder.append(", поверхность: ").append(surfaceLabel);
                    }
                    if (distance > 0) {
                        builder.append(String.format(Locale.getDefault(), ", расстояние: %.2f м", distance));
                    }
                    deviceLines.add(builder.toString());
                }
                if (deviceLines.isEmpty()) {
                    writeParagraph("Устройства не привязаны к группе.", 11f);
                } else {
                    writeBulletList(deviceLines, 11f);
                }
                writeParagraph(String.format(Locale.getDefault(),
                        "Суммарная длина кабеля: %.2f м", totalLength), 11f);

                MaterialGroup materialGroup = materialsByLabel.get(rawLabel);
                double gofraLength = calculateGofraLength(materialGroup);
                if (gofraLength > 0) {
                    writeParagraph(String.format(Locale.getDefault(),
                            "Метраж гофры: %.2f м", gofraLength), 11f);
                }
                if (materialGroup != null && materialGroup.getMaterials() != null && !materialGroup.getMaterials().isEmpty()) {
                    writeParagraph("Материалы:", 11f);
                    List<String> materials = new ArrayList<>();
                    for (MaterialUsage usage : materialGroup.getMaterials()) {
                        if (usage == null) {
                            continue;
                        }
                        StringBuilder line = new StringBuilder(safeText(usage.getMaterialName()));
                        if (StringUtils.hasText(usage.getAmount())) {
                            line.append(" — ").append(usage.getAmount().trim());
                        }
                        if (StringUtils.hasText(usage.getLayingSurface())) {
                            line.append(" (" + usage.getLayingSurface().trim() + ")");
                        }
                        materials.add(line.toString());
                    }
                    writeBulletList(materials, 11f);
                } else {
                    writeParagraph("Материалы группы не указаны.", 11f);
                }
                writeSpacing(18f);
            }
        }

        private Map<String, List<DeviceGroup>> groupDevices(List<DeviceGroup> deviceGroups) {
            Map<String, List<DeviceGroup>> map = new LinkedHashMap<>();
            if (deviceGroups == null) {
                return map;
            }
            for (DeviceGroup group : deviceGroups) {
                if (group == null) {
                    continue;
                }
                String label = normalizeLabel(group.getGroupLabel());
                map.computeIfAbsent(label, key -> new ArrayList<>()).add(group);
            }
            return map;
        }

        private Map<String, MaterialGroup> mapMaterials(List<MaterialGroup> materialGroups) {
            Map<String, MaterialGroup> map = new LinkedHashMap<>();
            if (materialGroups == null) {
                return map;
            }
            for (MaterialGroup group : materialGroups) {
                if (group == null) {
                    continue;
                }
                String label = normalizeLabel(group.getGroupLabel());
                if (!map.containsKey(label)) {
                    map.put(label, group);
                }
            }
            return map;
        }

        private double calculateGofraLength(MaterialGroup group) {
            if (group == null || group.getMaterials() == null) {
                return 0.0;
            }
            double total = 0.0;
            for (MaterialUsage usage : group.getMaterials()) {
                if (usage == null || !StringUtils.hasText(usage.getMaterialName())) {
                    continue;
                }
                String name = usage.getMaterialName().toLowerCase(Locale.ROOT);
                if (name.contains("гоф")) {
                    double length = parseNumeric(usage.getAmount());
                    if (!Double.isNaN(length)) {
                        total += length;
                    }
                }
            }
            return total;
        }

        private String determineGroupLabel(String groupLabel, String fallback) {
            String candidate = normalizeLabel(groupLabel);
            if (!StringUtils.hasText(candidate)) {
                candidate = normalizeLabel(fallback);
            }
            return StringUtils.hasText(candidate) ? candidate : "Без названия";
        }

        private String normalizeLabel(String value) {
            return StringUtils.hasText(value) ? value.trim() : "";
        }

        private double parseNumeric(String value) {
            if (!StringUtils.hasText(value)) {
                return Double.NaN;
            }
            String sanitized = value.replace(',', '.');
            StringBuilder builder = new StringBuilder();
            for (char ch : sanitized.toCharArray()) {
                if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '-') {
                    builder.append(ch);
                } else if (builder.length() > 0) {
                    break;
                }
            }
            if (builder.length() == 0) {
                return Double.NaN;
            }
            try {
                return Double.parseDouble(builder.toString());
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }

        private PDImageXObject createLogoImage(PDDocument document,
                                               ApplicationSettingsService.CompanyLogo logo) throws IOException {
            if (logo == null || logo.data() == null || logo.data().length == 0) {
                return null;
            }
            try {
                return PDImageXObject.createFromByteArray(document, logo.data(), "logo");
            } catch (IOException ex) {
                return null;
            }
        }

        private void drawLogo() throws IOException {
            if (logoImage == null) {
                return;
            }
            float imageWidth = logoImage.getWidth();
            float imageHeight = logoImage.getHeight();
            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }
            float maxWidth = pageWidth - 2 * MARGIN;
            float maxHeight = 80f;
            float scale = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
            if (scale <= 0) {
                scale = 1f;
            }
            float drawWidth = imageWidth * scale;
            float drawHeight = imageHeight * scale;
            float x = (pageWidth - drawWidth) / 2f;
            float topY = pageHeight - MARGIN - drawHeight;
            contentStream.drawImage(logoImage, x, topY, drawWidth, drawHeight);
            y = topY - 24f;
        }

        private String resolveSurfaceLabel(String category) {
            return SurfaceType.resolve(category)
                    .map(SurfaceType::getDisplayName)
                    .orElse(null);
        }

        private String formatQuantity(double value) {
            double rounded = Math.rint(value);
            if (Math.abs(value - rounded) < 1e-3) {
                return String.format(Locale.getDefault(), "%.0f", rounded);
            }
            return String.format(Locale.getDefault(), "%.2f", value);
        }

        private void writeSectionHeading(String text) throws IOException {
            writeParagraph(text, 18f);
            writeSpacing(8f);
        }

        private void writeSubHeading(String text) throws IOException {
            writeParagraph(text, 14f);
            writeSpacing(6f);
        }

        private void writeCentered(String text, float fontSize) throws IOException {
            ensureSpace(fontSize * LINE_SPACING_RATIO);
            float textWidth = font.getStringWidth(text) / 1000f * fontSize;
            float startX = Math.max((pageWidth - textWidth) / 2f, MARGIN);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(startX, y);
            contentStream.showText(text);
            contentStream.endText();
            y -= fontSize * LINE_SPACING_RATIO;
        }

        private void writeParagraph(String text, float fontSize) throws IOException {
            if (!StringUtils.hasText(text)) {
                return;
            }
            String[] paragraphs = text.split("\\r?\\n");
            for (String paragraph : paragraphs) {
                List<String> lines = wrapText(paragraph.trim(), fontSize, pageWidth - 2 * MARGIN);
                if (lines.isEmpty()) {
                    continue;
                }
                for (String line : lines) {
                    writeLine(line, fontSize, MARGIN);
                }
                y -= fontSize * 0.5f;
            }
        }

        private void writeBulletList(List<String> items, float fontSize) throws IOException {
            for (String item : items) {
                List<String> lines = wrapText(item, fontSize, pageWidth - 2 * MARGIN - 16f);
                if (lines.isEmpty()) {
                    continue;
                }
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (i == 0) {
                        writeLine("• " + line, fontSize, MARGIN);
                    } else {
                        writeLine(line, fontSize, MARGIN + 16f);
                    }
                }
            }
        }

        private void writeLine(String text, float fontSize, float offsetX) throws IOException {
            ensureSpace(fontSize * LINE_SPACING_RATIO);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(offsetX, y);
            contentStream.showText(text);
            contentStream.endText();
            y -= fontSize * LINE_SPACING_RATIO;
        }

        private void writeSpacing(float spacing) {
            y -= spacing;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed <= MARGIN) {
                newPage();
            }
        }

        private List<String> wrapText(String text, float fontSize, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            if (!StringUtils.hasText(text)) {
                return lines;
            }
            String[] words = text.trim().split("\\s+");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
                float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;
                if (candidateWidth > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(candidate);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            return lines;
        }

        private String safeText(String text) {
            return StringUtils.hasText(text) ? text.trim() : "—";
        }

        private double safeDouble(Double value) {
            return value == null ? 0.0 : Math.max(value, 0.0);
        }

        private void newPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            pageWidth = page.getMediaBox().getWidth();
            pageHeight = page.getMediaBox().getHeight();
            y = pageHeight - MARGIN;
        }

        void finish() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }

        @Override
        public void close() throws IOException {
            finish();
        }
    }
}
