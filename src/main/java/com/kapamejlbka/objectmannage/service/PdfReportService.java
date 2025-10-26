package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ManagedObject;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.MountingMaterial;
import com.kapamejlbka.objectmannage.model.PrimaryDataSnapshot.MountingRequirement;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.CableFunctionSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmannage.model.PrimaryDataSummary.MaterialUsageSummary;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            builder.addMountingMaterialsSection(safeSnapshot, summary, additional);
            builder.addEquipmentSection(summary);
            builder.addNodeMaterialsSection(summary);
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
                                         PrimaryDataSummary summary,
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
                    StringBuilder builder = new StringBuilder();
                    builder.append(element);
                    builder.append(" — ");
                    builder.append(StringUtils.hasText(quantity) ? quantity : "не указано");
                    List<MountingMaterial> materials = requirement.getMaterials();
                    if (materials != null) {
                        List<String> materialDetails = new ArrayList<>();
                        for (MountingMaterial material : materials) {
                            if (material == null) {
                                continue;
                            }
                            String name = safeText(material.getMaterialName());
                            if (!StringUtils.hasText(name)) {
                                continue;
                            }
                            String amount = safeText(material.getAmount());
                            String unit = safeText(material.getUnit());
                            StringBuilder materialBuilder = new StringBuilder(name);
                            if (StringUtils.hasText(amount)) {
                                materialBuilder.append(" — ").append(amount);
                            } else if (StringUtils.hasText(unit)) {
                                materialBuilder.append(" (" + unit + ")");
                            }
                            materialDetails.add(materialBuilder.toString());
                        }
                        if (!materialDetails.isEmpty()) {
                            builder.append("; материалы: ").append(String.join(", ", materialDetails));
                        }
                    }
                    items.add(builder.toString());
                }
                writeBulletList(items, 12f);
            }

            if (summary != null && summary.getMountingElementTotals() != null
                    && !summary.getMountingElementTotals().isEmpty()) {
                writeSpacing(18f);
                writeSubHeading("Суммарные монтажные элементы");
                List<String> totals = new ArrayList<>();
                for (PrimaryDataSummary.MaterialTotal total : summary.getMountingElementTotals()) {
                    if (total == null) {
                        continue;
                    }
                    totals.add(formatTotalLine(total));
                }
                if (totals.isEmpty()) {
                    totals.add("Монтажные элементы не рассчитаны.");
                }
                writeBulletList(totals, 11f);
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

        void addNodeMaterialsSection(PrimaryDataSummary summary) throws IOException {
            newPage();
            writeSectionHeading("Материалы по узлам");
            if (summary == null || summary.getNodeSummaries() == null || summary.getNodeSummaries().isEmpty()) {
                writeParagraph("Материалы по узлам не указаны.", 12f);
                return;
            }
            for (PrimaryDataSummary.NodeSummary node : summary.getNodeSummaries()) {
                if (node == null) {
                    continue;
                }
                writeSubHeading("Узел: " + safeText(node.getName()));
                List<String> info = new ArrayList<>();
                if (StringUtils.hasText(node.getMountingElementName())) {
                    info.add("Монтажный элемент: " + node.getMountingElementName().trim());
                }
                if (StringUtils.hasText(node.getPowerCableTypeName())) {
                    info.add("Кабель: " + node.getPowerCableTypeName().trim());
                }
                if (node.getDistanceToPower() != null && node.getDistanceToPower() > 0) {
                    info.add(String.format(Locale.getDefault(), "Расстояние до питания: %.2f м", node.getDistanceToPower()));
                }
                if (node.getSingleSocketCount() > 0 || node.getDoubleSocketCount() > 0) {
                    info.add(String.format(Locale.getDefault(), "Розетки: %d одноместн., %d двухместн.",
                            node.getSingleSocketCount(), node.getDoubleSocketCount()));
                }
                if (node.getBreakerCount() > 0) {
                    info.add(String.format(Locale.getDefault(), "Автоматические выключатели: %d шт.", node.getBreakerCount()));
                }
                if (node.getBreakerBoxCount() > 0) {
                    info.add(String.format(Locale.getDefault(), "Боксы под автоматы: %d шт.", node.getBreakerBoxCount()));
                }
                if (node.getNshviCount() > 0) {
                    info.add(String.format(Locale.getDefault(), "Наконечники НШВИ: %d шт.", node.getNshviCount()));
                }
                if (!info.isEmpty()) {
                    writeBulletList(info, 11f);
                }
                if (node.getMaterialTotals() != null && !node.getMaterialTotals().isEmpty()) {
                    writeParagraph("Итого по узлу:", 11f);
                    List<String> totals = new ArrayList<>();
                    for (PrimaryDataSummary.MaterialTotal total : node.getMaterialTotals()) {
                        if (total == null) {
                            continue;
                        }
                        totals.add(formatTotalLine(total));
                    }
                    writeBulletList(totals, 11f);
                }
                if (node.getMaterialGroups() != null && !node.getMaterialGroups().isEmpty()) {
                    for (PrimaryDataSummary.NodeMaterialGroupSummary group : node.getMaterialGroups()) {
                        if (group == null) {
                            continue;
                        }
                        writeParagraph("Группа: " + safeText(group.getLabel()), 11f);
                        List<String> materials = new ArrayList<>();
                        if (group.getMaterials() != null) {
                            for (MaterialUsageSummary usage : group.getMaterials()) {
                                if (usage == null) {
                                    continue;
                                }
                                StringBuilder line = new StringBuilder(safeText(usage.getMaterialName()));
                                if (StringUtils.hasText(usage.getAmountWithUnit())) {
                                    line.append(" — ").append(usage.getAmountWithUnit().trim());
                                }
                                if (StringUtils.hasText(usage.getSurfaceLabel())) {
                                    line.append(" (" + usage.getSurfaceLabel().trim() + ")");
                                }
                                materials.add(line.toString());
                            }
                        }
                        if (materials.isEmpty()) {
                            materials.add("Материалы не указаны.");
                        }
                        writeBulletList(materials, 11f);
                        writeSpacing(6f);
                    }
                } else if (node.getMaterialTotals() == null || node.getMaterialTotals().isEmpty()) {
                    writeParagraph("Материалы не указаны.", 11f);
                }
                writeSpacing(12f);
            }
            if (summary.getMaterialTotals() != null && !summary.getMaterialTotals().isEmpty()) {
                writeSubHeading("Суммарные материалы по объекту");
                List<String> totals = new ArrayList<>();
                for (PrimaryDataSummary.MaterialTotal total : summary.getMaterialTotals()) {
                    if (total == null) {
                        continue;
                    }
                    totals.add(formatTotalLine(total));
                }
                writeBulletList(totals, 11f);
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

        private String formatQuantity(double value) {
            double rounded = Math.rint(value);
            if (Math.abs(value - rounded) < 1e-3) {
                return String.format(Locale.getDefault(), "%.0f", rounded);
            }
            return String.format(Locale.getDefault(), "%.2f", value);
        }

        private String formatTotalLine(PrimaryDataSummary.MaterialTotal total) {
            if (total == null) {
                return "—";
            }
            String name = safeText(total.getName());
            String quantity = formatQuantity(total.getQuantity());
            String unit = safeText(total.getUnit());
            if (StringUtils.hasText(unit)) {
                return String.format(Locale.getDefault(), "%s — %s %s", name, quantity, unit);
            }
            return String.format(Locale.getDefault(), "%s — %s", name, quantity);
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
