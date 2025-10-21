package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@Service
public class FilePreviewService {

    private static final int MAX_PREVIEW_ROWS = 50;
    private static final int MAX_PREVIEW_COLUMNS = 20;

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

    private String renderExcelPreview(StoredFile storedFile) throws IOException, InvalidFormatException {
        try (InputStream inputStream = storageService.loadAsResource(storedFile).getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                return wrapInDocument("<p class=\"preview-empty\">Лист в файле отсутствует.</p>");
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
                return wrapInDocument("<p class=\"preview-empty\">Файл не содержит данных для предпросмотра.</p>");
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

            return wrapInDocument(content.toString());
        }
    }

    private String wrapInDocument(String content) {
        return "<!DOCTYPE html>"
                + "<html lang=\"ru\">"
                + "<head>"
                + "<meta charset=\"UTF-8\"/>"
                + "<style>"
                + "body{font-family:system-ui,-apple-system,Segoe UI,sans-serif;margin:0;padding:1rem;background:#f9fafb;color:#111827;}"
                + ".excel-preview-table{width:100%;border-collapse:collapse;background:#fff;box-shadow:inset 0 0 0 1px #e5e7eb;font-size:14px;}"
                + ".excel-preview-table td{border:1px solid #e5e7eb;padding:0.35rem 0.5rem;max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}"
                + ".preview-empty{margin:0;color:#6b7280;}"
                + ".preview-hint{margin-top:0.75rem;font-size:12px;color:#6b7280;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + content
                + "</body>"
                + "</html>";
    }
}

