package com.kapamejlbka.objectmannage.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class StoredFile {

    private final UUID id;
    private final String originalFilename;
    private final String storedFilename;
    private final long size;
    private final LocalDateTime uploadedAt;

    public StoredFile(UUID id, String originalFilename, String storedFilename, long size, LocalDateTime uploadedAt) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
