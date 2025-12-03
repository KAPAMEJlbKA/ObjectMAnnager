package com.kapamejlbka.objectmanager.domain.customer;

import com.kapamejlbka.objectmanager.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "object_changes")
public class ObjectChange {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "object_id", nullable = true)
    private ManagedObject managedObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    private LocalDateTime changedAt;

    @Enumerated(EnumType.STRING)
    private ObjectChangeType changeType;

    private String fieldName;

    @Lob
    private String oldValue;

    @Lob
    private String newValue;

    @Column(length = 1024)
    private String summary;

    public ObjectChange() {
    }

    public ObjectChange(ObjectChangeType changeType, String fieldName, String oldValue, String newValue, String summary) {
        this.changeType = changeType;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.summary = summary;
        this.changedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public ManagedObject getManagedObject() {
        return managedObject;
    }

    public void setManagedObject(ManagedObject managedObject) {
        this.managedObject = managedObject;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public ObjectChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ObjectChangeType changeType) {
        this.changeType = changeType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
