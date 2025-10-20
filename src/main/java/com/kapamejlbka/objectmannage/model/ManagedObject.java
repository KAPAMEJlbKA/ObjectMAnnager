package com.kapamejlbka.objectmannage.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ManagedObject {

    private final UUID id;
    private final LocalDateTime createdAt;
    private String name;
    private String description;
    private String primaryData;
    private final List<StoredFile> files = new ArrayList<>();

    public ManagedObject(UUID id, String name, String description, String primaryData) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.primaryData = primaryData;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimaryData() {
        return primaryData;
    }

    public void setPrimaryData(String primaryData) {
        this.primaryData = primaryData;
    }

    public List<StoredFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(StoredFile storedFile) {
        files.add(storedFile);
    }
}
