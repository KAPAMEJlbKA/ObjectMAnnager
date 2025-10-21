package com.kapamejlbka.objectmannage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue
    private UUID id;

    private String originalFilename;

    private String storedFilename;

    private long size;

    private LocalDateTime uploadedAt;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id")
    private ManagedObject managedObject;

    public StoredFile() {
    }

    public StoredFile(String originalFilename, String storedFilename, long size, LocalDateTime uploadedAt, String contentType) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.size = size;
        this.uploadedAt = uploadedAt;
        this.contentType = contentType;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        if (originalFilename == null) {
            return "";
        }

        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(lastDotIndex + 1);
    }

    public boolean isImageType() {
        String normalizedContentType = normalizeContentType();
        return !normalizedContentType.isEmpty() && normalizedContentType.startsWith("image/");
    }

    public boolean isPdfType() {
        return "application/pdf".equals(normalizeContentType());
    }

    public boolean isExcelType() {
        String normalizedContentType = normalizeContentType();
        if (normalizedContentType.startsWith("application/vnd.ms-excel")
                || normalizedContentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
            return true;
        }

        String extension = normalizeExtension();
        return "xls".equals(extension) || "xlsx".equals(extension);
    }

    public boolean isWordType() {
        String normalizedContentType = normalizeContentType();
        if ("application/msword".equals(normalizedContentType)
                || normalizedContentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
            return true;
        }

        String extension = normalizeExtension();
        return "doc".equals(extension) || "docx".equals(extension);
    }

    public boolean hasCustomPreview() {
        return isExcelType() || isWordType();
    }

    public boolean isUnknownType() {
        return !isImageType() && !isPdfType() && !isExcelType() && !isWordType();
    }

    public ManagedObject getManagedObject() {
        return managedObject;
    }

    public void setManagedObject(ManagedObject managedObject) {
        this.managedObject = managedObject;
    }

    private String normalizeContentType() {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String normalizeExtension() {
        String extension = getExtension();
        return extension.isEmpty() ? "" : extension.toLowerCase(Locale.ROOT);
    }
}
