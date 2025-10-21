package com.kapamejlbka.objectmannage.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "managed_objects")
public class ManagedObject {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2048)
    private String description;

    @Column(length = 4000)
    private String primaryData;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private ProjectCustomer customer;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean deletionRequested;

    private LocalDateTime deletionRequestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deletion_requested_by")
    private UserAccount deletionRequestedBy;

    @OneToMany(mappedBy = "managedObject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoredFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "managedObject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectChange> changes = new ArrayList<>();

    public ManagedObject() {
    }

    public ManagedObject(String name, String description, String primaryData,
                         ProjectCustomer customer, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.primaryData = primaryData;
        this.customer = customer;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
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

    public ProjectCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(ProjectCustomer customer) {
        this.customer = customer;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeletionRequested() {
        return deletionRequested;
    }

    public void setDeletionRequested(boolean deletionRequested) {
        this.deletionRequested = deletionRequested;
    }

    public LocalDateTime getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) {
        this.deletionRequestedAt = deletionRequestedAt;
    }

    public UserAccount getDeletionRequestedBy() {
        return deletionRequestedBy;
    }

    public void setDeletionRequestedBy(UserAccount deletionRequestedBy) {
        this.deletionRequestedBy = deletionRequestedBy;
    }

    public List<StoredFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(StoredFile storedFile) {
        files.add(storedFile);
        storedFile.setManagedObject(this);
    }

    public void removeFile(StoredFile storedFile) {
        files.remove(storedFile);
        storedFile.setManagedObject(null);
    }

    public List<ObjectChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public void addChange(ObjectChange change) {
        changes.add(change);
        change.setManagedObject(this);
    }

    public void removeChange(ObjectChange change) {
        changes.remove(change);
        change.setManagedObject(null);
    }
}
