package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@Service
public class FilePreviewService {

    private static final int MAX_PREVIEW_ROWS = 50;
    private static final int MAX_PREVIEW_COLUMNS = 20;
    private static final int MAX_WORD_PARAGRAPHS = 120;
    private static final int MAX_WORD_CHARACTERS = 6000;
    private static final String EXCEL_STYLES = ""
            + ".excel-preview-table{width:100%;border-collapse:collapse;background:#fff;box-shadow:inset 0 0 0 1px #e5e7eb;font-size:14px;}"
            + ".excel-preview-table td{border:1px solid #e5e7eb;padding:0.35rem 0.5rem;max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}"
            + ".preview-empty{margin:0;color:#6b7280;}"
            + ".preview-hint{margin-top:0.75rem;font-size:12px;color:#6b7280;}";
    private static final String WORD_STYLES = ""
            + ".word-preview{background:#fff;border-radius:8px;padding:1.25rem;box-shadow:inset 0 0 0 1px #e5e7eb;line-height:1.6;font-size:15px;}"
            + ".word-preview p{margin:0 0 1rem;white-space:pre-wrap;word-break:break-word;}"
            + ".word-preview__gap{height:0.75rem;}"
            + ".preview-empty{margin:0;color:#6b7280;}"
            + ".preview-hint{margin-top:0.75rem;font-size:12px;color:#6b7280;}";

    private final FileStorageService storageService;

    public FilePreviewService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public String renderPreview(StoredFile storedFile) {
        String contentType = storedFile.getContentType();
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Не удалось определить тип файла");
        }

        if (isExcel(contentType, storedFile)) {
            try {
                return renderExcelPreview(storedFile);
            } catch (IOException | InvalidFormatException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось подготовить предпросмотр Excel файла", ex);
            }
        }

        if (isWord(contentType, storedFile)) {
            try {
                return renderWordPreview(storedFile);
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось подготовить предпросмотр Word файла", ex);
            }
        }

        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Предпросмотр для данного типа файла не поддерживается");
    }

    private boolean isExcel(String contentType, StoredFile storedFile) {
        if (contentType.startsWith("application/vnd.ms-excel")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
            return true;
        }

        String extension = storedFile.getExtension();
        return "xls".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension);
    }

    private boolean isWord(String contentType, StoredFile storedFile) {
        if (contentType != null && (contentType.equalsIgnoreCase("application/msword")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"))) {
            return true;
        }

        String extension = storedFile.getExtension();
        return "doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension);
    }

    private String renderExcelPreview(StoredFile storedFile) throws IOException, InvalidFormatException {
        try (InputStream inputStream = storageService.loadAsResource(storedFile).getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                return wrapInDocument("<p class=\"preview-empty\">Лист в файле отсутствует.</p>", EXCEL_STYLES);
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.getDefault());
            StringBuilder tableBuilder = new StringBuilder();
            tableBuilder.append("<table class=\"excel-preview-table\">");

            int rowsRendered = 0;
            boolean hasMoreColumns = false;
            for (Row row : sheet) {
                if (rowsRendered >= MAX_PREVIEW_ROWS) {
                    break;
                }
                tableBuilder.append("<tr>");
                int physicalLastColumn = row.getLastCellNum();
                if (physicalLastColumn < 0) {
                    physicalLastColumn = 0;
                }
                if (physicalLastColumn > MAX_PREVIEW_COLUMNS) {
                    hasMoreColumns = true;
                }
                int lastColumn = Math.min(physicalLastColumn, MAX_PREVIEW_COLUMNS);
                for (int columnIndex = 0; columnIndex < lastColumn; columnIndex++) {
                    Cell cell = row.getCell(columnIndex);
                    String value = cell != null ? formatter.formatCellValue(cell) : "";
                    tableBuilder.append("<td>")
                            .append(HtmlUtils.htmlEscape(value))
                            .append("</td>");
                }
                if (lastColumn == 0) {
                    tableBuilder.append("<td></td>");
                }
                tableBuilder.append("</tr>");
                rowsRendered++;
            }

            if (rowsRendered == 0) {
                return wrapInDocument("<p class=\"preview-empty\">Файл не содержит данных для предпросмотра.</p>", EXCEL_STYLES);
            }

            tableBuilder.append("</table>");

            boolean hasMoreRows = sheet.getPhysicalNumberOfRows() > rowsRendered;

            StringBuilder content = new StringBuilder();
            content.append(tableBuilder);
            if (hasMoreRows || hasMoreColumns) {
                content.append("<p class=\"preview-hint\">");
                content.append("Показаны только первые ").append(rowsRendered).append(" строк");
                if (hasMoreColumns) {
                    content.append(" и ").append(MAX_PREVIEW_COLUMNS).append(" колонок");
                }
                content.append(".</p>");
            }

            return wrapInDocument(content.toString(), EXCEL_STYLES);
        }
    }

    private String renderWordPreview(StoredFile storedFile) throws IOException {
        boolean docx = storedFile.getExtension().equalsIgnoreCase("docx")
                || (storedFile.getContentType() != null
                && storedFile.getContentType().startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"));

        String text;
        try (InputStream inputStream = storageService.loadAsResource(storedFile).getInputStream()) {
            if (docx) {
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    text = extractor.getText();
                }
            } else {
                try (HWPFDocument document = new HWPFDocument(inputStream);
                     WordExtractor extractor = new WordExtractor(document)) {
                    text = extractor.getText();
                }
            }
        }

        String cleaned = text == null ? "" : text.replace('\u0007', ' ').trim();
        if (cleaned.isEmpty()) {
            return wrapInDocument("<p class=\"preview-empty\">Документ не содержит текста для предпросмотра.</p>",
                    WORD_STYLES);
        }

        String[] paragraphs = cleaned.split("\\R");
        StringBuilder content = new StringBuilder();
        content.append("<div class=\"word-preview\">");
        int paragraphsRendered = 0;
        int charactersRendered = 0;
        boolean truncated = false;
        for (String paragraph : paragraphs) {
            if (paragraphsRendered >= MAX_WORD_PARAGRAPHS || charactersRendered >= MAX_WORD_CHARACTERS) {
                truncated = true;
                break;
            }

            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                content.append("<p class=\"word-preview__gap\">&nbsp;</p>");
                paragraphsRendered++;
                continue;
            }

            int remaining = MAX_WORD_CHARACTERS - charactersRendered;
            String toRender = trimmed.length() > remaining ? trimmed.substring(0, remaining) : trimmed;
            if (toRender.length() < trimmed.length()) {
                truncated = true;
            }

            content.append("<p>")
                    .append(HtmlUtils.htmlEscape(toRender))
                    .append("</p>");

            charactersRendered += toRender.length();
            paragraphsRendered++;
        }
        content.append("</div>");

        if (truncated || paragraphsRendered < paragraphs.length) {
            content.append("<p class=\"preview-hint\">Показаны первые ")
                    .append(paragraphsRendered)
                    .append(" абзацев документа.</p>");
        }

        return wrapInDocument(content.toString(), WORD_STYLES);
    }

    private String wrapInDocument(String content, String extraStyles) {
        return "<!DOCTYPE html>"
                + "<html lang=\"ru\">"
                + "<head>"
                + "<meta charset=\"UTF-8\"/>"
                + "<style>"
                + "body{font-family:system-ui,-apple-system,Segoe UI,sans-serif;margin:0;padding:1rem;background:#f9fafb;color:#111827;}"
                + (extraStyles == null ? "" : extraStyles)
                + "</style>"
                + "</head>"
                + "<body>"
                + content
                + "</body>"
                + "</html>";
    }
}

