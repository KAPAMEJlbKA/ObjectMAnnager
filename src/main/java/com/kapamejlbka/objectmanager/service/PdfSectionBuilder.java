package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.ManagedObject;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.MountingMaterial;
import com.kapamejlbka.objectmanager.model.PrimaryDataSnapshot.MountingRequirement;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.AdditionalMaterialItem;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.CableFunctionSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.DeviceTypeSummary;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.MaterialTotal;
import com.kapamejlbka.objectmanager.model.PrimaryDataSummary.MaterialUsageSummary;
import com.kapamejlbka.objectmanager.service.ApplicationSettingsService;
import com.kapamejlbka.objectmanager.service.ApplicationSettingsService.CompanyLogo;
import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PdfSectionBuilder {

    private final ApplicationSettingsService applicationSettingsService;

    public PdfSectionBuilder(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService;
    }

    public void buildSections(PDDocument document,
                               PDFont font,
                               ManagedObject object,
                               PrimaryDataSnapshot snapshot,
                               PrimaryDataSummary summary,
                               List<AdditionalMaterialItem> additionalMaterials) throws IOException {
        PrimaryDataSnapshot safeSnapshot = snapshot != null ? snapshot : new PrimaryDataSnapshot();
        List<AdditionalMaterialItem> calculated = additionalMaterials != null ? additionalMaterials : List.of();
        ApplicationSettingsService.CompanyLogo logo = applicationSettingsService.getCompanyLogo().orElse(null);
        try (PdfDocumentBuilder builder = new PdfDocumentBuilder(document, font, logo)) {
            builder.addTitlePage(object, summary);
            builder.addMaterialLedger(object, safeSnapshot, summary, calculated);
            builder.addMountingMaterialsSection(safeSnapshot, summary, calculated);
            builder.addEquipmentSection(summary);
            builder.addNodeMaterialsSection(summary);
        }
    }

    private static class PdfDocumentBuilder implements Closeable {
        private static final float MARGIN = 54f;
        private static final float LINE_SPACING_RATIO = 1.4f;
        private static final Pattern QUANTITY_PATTERN = Pattern.compile(
                "^(\\d+(?:[\\s\\u00A0]?\\d{3})*(?:[\\.,]\\d+)?)\\s*(.*)$");

        private final PDDocument document;
        private final PDFont font;
        private final PDImageXObject logoImage;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;
        private float pageWidth;
        private float pageHeight;

        PdfDocumentBuilder(PDDocument document, PDFont font, CompanyLogo logo) throws IOException {
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

        void addMaterialLedger(ManagedObject object,
                               PrimaryDataSnapshot snapshot,
                               PrimaryDataSummary summary,
                               List<AdditionalMaterialItem> calculatedMaterials) throws IOException {
            newPage();
            String objectName = safeText(object.getName());
            writeParagraph(String.format("Кабельный журнал для объекта \"%s\"", objectName), 18f);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            writeParagraph("Дата формирования: " + LocalDateTime.now().format(formatter), 12f);
            if (summary != null && summary.isHasData()) {
                String connectionPoints = summary.getDeclaredConnectionAssignments() != null
                        ? summary.getDeclaredConnectionAssignments().toString()
                        : "0";
                writeParagraph(String.format(Locale.getDefault(),
                        "Устройства: %d • Узлы: %d • Точки подключения: %s",
                        summary.getTotalDeviceCount(),
                        summary.getTotalNodes(),
                        connectionPoints), 12f);
            }
            writeSpacing(18f);

            List<TableRow> rows = collectMaterialLedgerRows(snapshot, summary, calculatedMaterials);
            if (rows.isEmpty()) {
                writeParagraph("Материалы не указаны.", 12f);
                return;
            }

            String[] headers = {"Раздел", "Наименование", "Количество", "Ед. изм.", "Примечание"};
            List<String[]> tableRows = new ArrayList<>();
            for (TableRow row : rows) {
                if (row == null) {
                    continue;
                }
                tableRows.add(new String[]{
                        row.category(),
                        row.name(),
                        defaultValue(row.quantity()),
                        defaultValue(row.unit()),
                        defaultValue(row.note())
                });
            }

            float availableWidth = pageWidth - 2 * MARGIN;
            float[] columnWidths = new float[]{
                    availableWidth * 0.22f,
                    availableWidth * 0.34f,
                    availableWidth * 0.12f,
                    availableWidth * 0.10f,
                    0f
            };
            columnWidths[4] = availableWidth - (columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3]);
            writeTable(headers, tableRows, columnWidths, 11f);
            writeSpacing(12f);
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

        private List<TableRow> collectMaterialLedgerRows(PrimaryDataSnapshot snapshot,
                                                         PrimaryDataSummary summary,
                                                         List<AdditionalMaterialItem> calculatedMaterials) {
            List<TableRow> rows = new ArrayList<>();
            if (summary != null) {
                List<TableRow> cableRows = new ArrayList<>();
                if (summary.getCableLengthSummaries() != null) {
                    for (PrimaryDataSummary.CableLengthSummary cable : summary.getCableLengthSummaries()) {
                        if (cable == null) {
                            continue;
                        }
                        String note = cable.isClassificationMissing() ? "Нет классификации" : null;
                        cableRows.add(new TableRow(
                                "",
                                trimToNull(cable.getCableTypeName()),
                                formatQuantity(cable.getTotalLength()),
                                "м",
                                trimToNull(note)));
                    }
                }
                appendGroup(rows, "Метраж кабелей", cableRows);

                List<TableRow> materialTotals = new ArrayList<>();
                if (summary.getMaterialTotals() != null) {
                    for (PrimaryDataSummary.MaterialTotal total : summary.getMaterialTotals()) {
                        if (total == null) {
                            continue;
                        }
                        if (total.getQuantity() <= 0) {
                            continue;
                        }
                        materialTotals.add(new TableRow(
                                "",
                                trimToNull(total.getName()),
                                formatQuantity(total.getQuantity()),
                                trimToNull(total.getUnit()),
                                null));
                    }
                }
                appendGroup(rows, "Материалы по объекту", materialTotals);

                List<TableRow> mountingTotals = new ArrayList<>();
                if (summary.getMountingElementTotals() != null) {
                    for (PrimaryDataSummary.MaterialTotal total : summary.getMountingElementTotals()) {
                        if (total == null) {
                            continue;
                        }
                        if (total.getQuantity() <= 0) {
                            continue;
                        }
                        mountingTotals.add(new TableRow(
                                "",
                                trimToNull(total.getName()),
                                formatQuantity(total.getQuantity()),
                                trimToNull(total.getUnit()),
                                null));
                    }
                }
                appendGroup(rows, "Монтажные элементы", mountingTotals);
            }

            List<TableRow> cabinetRows = new ArrayList<>();
            if (snapshot != null && snapshot.getMountingElements() != null) {
                for (PrimaryDataSnapshot.MountingRequirement requirement : snapshot.getMountingElements()) {
                    if (requirement == null) {
                        continue;
                    }
                    QuantityDescriptor quantity = parseQuantityDescriptor(requirement.getQuantity());
                    String note = mergeNotes(buildRequirementNote(requirement), quantity.note());
                    cabinetRows.add(new TableRow(
                            "",
                            trimToNull(requirement.getElementName()),
                            quantity.quantity(),
                            quantity.unit(),
                            trimToNull(note)));
                }
            }
            appendGroup(rows, "Назначенные шкафы", cabinetRows);

            List<TableRow> additionalRows = new ArrayList<>();
            if (calculatedMaterials != null) {
                for (AdditionalMaterialItem item : calculatedMaterials) {
                    if (item == null || item.getQuantity() <= 0) {
                        continue;
                    }
                    additionalRows.add(new TableRow(
                            "",
                            trimToNull(item.getName()),
                            formatQuantity(item.getQuantity()),
                            trimToNull(item.getUnit()),
                            null));
                }
            }
            appendGroup(rows, "Дополнительные материалы", additionalRows);

            rows.removeIf(this::isRowEmpty);
            return rows;
        }

        private void appendGroup(List<TableRow> target, String label, List<TableRow> groupRows) {
            if (groupRows == null || groupRows.isEmpty()) {
                return;
            }
            boolean first = true;
            for (TableRow row : groupRows) {
                if (row == null) {
                    continue;
                }
                target.add(row.withCategory(first ? label : ""));
                first = false;
            }
        }

        private QuantityDescriptor parseQuantityDescriptor(String raw) {
            if (!StringUtils.hasText(raw)) {
                return new QuantityDescriptor(null, null, null);
            }
            String trimmed = raw.trim();
            Matcher matcher = QUANTITY_PATTERN.matcher(trimmed.replace(',', '.'));
            if (!matcher.matches()) {
                return new QuantityDescriptor(null, null, trimmed);
            }
            String numericPart = matcher.group(1).replace("\u00A0", "").replace(" ", "");
            Double amount = null;
            try {
                amount = Double.parseDouble(numericPart);
            } catch (NumberFormatException ignored) {
            }
            String remainder = matcher.group(2) != null ? matcher.group(2).trim() : null;
            String unit = null;
            String note = null;
            if (StringUtils.hasText(remainder)) {
                String normalized = remainder.trim();
                if (normalized.startsWith("(")) {
                    note = normalized;
                } else {
                    int spaceIndex = normalized.indexOf(' ');
                    if (spaceIndex > 0) {
                        unit = normalized.substring(0, spaceIndex).trim();
                        String tail = normalized.substring(spaceIndex).trim();
                        if (StringUtils.hasText(tail)) {
                            note = tail;
                        }
                    } else {
                        unit = normalized;
                    }
                }
            }
            String quantity = amount != null ? formatQuantity(amount) : null;
            return new QuantityDescriptor(quantity, unit, note);
        }

        private String buildRequirementNote(PrimaryDataSnapshot.MountingRequirement requirement) {
            if (requirement.getMaterials() == null || requirement.getMaterials().isEmpty()) {
                return null;
            }
            List<String> details = new ArrayList<>();
            for (PrimaryDataSnapshot.MountingMaterial material : requirement.getMaterials()) {
                if (material == null) {
                    continue;
                }
                String name = trimToNull(material.getMaterialName());
                if (name == null) {
                    continue;
                }
                String amount = trimToNull(material.getAmount());
                String unit = trimToNull(material.getUnit());
                StringBuilder builder = new StringBuilder(name);
                if (StringUtils.hasText(amount)) {
                    builder.append(" — ").append(amount);
                } else if (StringUtils.hasText(unit)) {
                    builder.append(" (").append(unit).append(")");
                }
                details.add(builder.toString());
            }
            if (details.isEmpty()) {
                return null;
            }
            return "Материалы: " + String.join("; ", details);
        }

        private boolean isRowEmpty(TableRow row) {
            if (row == null) {
                return true;
            }
            return !StringUtils.hasText(row.name())
                    && !StringUtils.hasText(row.quantity())
                    && !StringUtils.hasText(row.unit())
                    && !StringUtils.hasText(row.note());
        }

        private String mergeNotes(String first, String second) {
            String primary = trimToNull(first);
            String secondary = trimToNull(second);
            if (!StringUtils.hasText(primary)) {
                return secondary;
            }
            if (!StringUtils.hasText(secondary)) {
                return primary;
            }
            return primary + "; " + secondary;
        }

        private String trimToNull(String value) {
            return StringUtils.hasText(value) ? value.trim() : null;
        }

        private String defaultValue(String value) {
            if (value == null) {
                return "—";
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? "" : trimmed;
        }

        private void writeTable(String[] headers,
                                List<String[]> rows,
                                float[] columnWidths,
                                float fontSize) throws IOException {
            if (headers == null || headers.length == 0 || rows == null || rows.isEmpty()) {
                return;
            }
            float cellPadding = 6f;
            RowLayout headerLayout = prepareRow(headers, columnWidths, fontSize, cellPadding);
            ensureSpace(headerLayout.height());
            drawRow(headerLayout, columnWidths, fontSize, cellPadding, true, true, false);
            boolean shade = false;
            for (String[] row : rows) {
                if (row == null) {
                    continue;
                }
                RowLayout layout = prepareRow(row, columnWidths, fontSize, cellPadding);
                float previousY = y;
                ensureSpace(layout.height());
                if (y > previousY + 0.1f) {
                    headerLayout = prepareRow(headers, columnWidths, fontSize, cellPadding);
                    ensureSpace(headerLayout.height());
                    drawRow(headerLayout, columnWidths, fontSize, cellPadding, true, true, false);
                    shade = false;
                    ensureSpace(layout.height());
                }
                drawRow(layout, columnWidths, fontSize, cellPadding, false, false, shade);
                shade = !shade;
            }
        }

        private RowLayout prepareRow(String[] values,
                                     float[] columnWidths,
                                     float fontSize,
                                     float cellPadding) throws IOException {
            List<List<String>> columns = new ArrayList<>();
            float lineHeight = fontSize * LINE_SPACING_RATIO;
            int maxLines = 1;
            for (int i = 0; i < columnWidths.length; i++) {
                String value = i < values.length ? values[i] : "";
                float maxWidth = Math.max(columnWidths[i] - cellPadding * 2, 24f);
                List<String> lines = wrapText(value, fontSize, maxWidth);
                if (lines.isEmpty()) {
                    lines = List.of(" ");
                }
                columns.add(lines);
                maxLines = Math.max(maxLines, lines.size());
            }
            float height = maxLines * lineHeight + cellPadding * 2;
            return new RowLayout(columns, height);
        }

        private void drawRow(RowLayout layout,
                              float[] columnWidths,
                              float fontSize,
                              float cellPadding,
                              boolean header,
                              boolean drawTopBorder,
                              boolean shaded) throws IOException {
            float rowTop = y;
            float rowHeight = layout.height();
            float totalWidth = 0f;
            for (float width : columnWidths) {
                totalWidth += width;
            }
            if (header) {
                contentStream.saveGraphicsState();
                contentStream.setNonStrokingColor(240, 240, 240);
                contentStream.addRect(MARGIN, rowTop - rowHeight, totalWidth, rowHeight);
                contentStream.fill();
                contentStream.restoreGraphicsState();
            } else if (shaded) {
                contentStream.saveGraphicsState();
                contentStream.setNonStrokingColor(248, 248, 248);
                contentStream.addRect(MARGIN, rowTop - rowHeight, totalWidth, rowHeight);
                contentStream.fill();
                contentStream.restoreGraphicsState();
            }

            float lineHeight = fontSize * LINE_SPACING_RATIO;
            float textBase = rowTop - cellPadding - fontSize;
            float x = MARGIN;
            for (int i = 0; i < layout.cellLines().size(); i++) {
                List<String> cellLines = layout.cellLines().get(i);
                float currentY = textBase;
                for (String line : cellLines) {
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(x + cellPadding, currentY);
                    contentStream.showText(line != null ? line : "");
                    contentStream.endText();
                    currentY -= lineHeight;
                }
                x += columnWidths[i];
            }

            contentStream.saveGraphicsState();
            contentStream.setLineWidth(0.5f);
            if (drawTopBorder) {
                contentStream.moveTo(MARGIN, rowTop);
                contentStream.lineTo(MARGIN + totalWidth, rowTop);
            }
            contentStream.moveTo(MARGIN, rowTop - rowHeight);
            contentStream.lineTo(MARGIN + totalWidth, rowTop - rowHeight);
            float currentX = MARGIN;
            contentStream.moveTo(currentX, rowTop);
            contentStream.lineTo(currentX, rowTop - rowHeight);
            for (float columnWidth : columnWidths) {
                currentX += columnWidth;
                contentStream.moveTo(currentX, rowTop);
                contentStream.lineTo(currentX, rowTop - rowHeight);
            }
            contentStream.stroke();
            contentStream.restoreGraphicsState();
            y = rowTop - rowHeight;
        }

        private record RowLayout(List<List<String>> cellLines, float height) {
        }

        private record TableRow(String category, String name, String quantity, String unit, String note) {
            TableRow withCategory(String category) {
                return new TableRow(category, name, quantity, unit, note);
            }
        }

        private record QuantityDescriptor(String quantity, String unit, String note) {
        }

        private PDImageXObject createLogoImage(PDDocument document,
                                               CompanyLogo logo) throws IOException {
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
