package com.kapamejlbka.objectmanager.domain.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_settings")
public class CalculationSettings {

    @Id
    private Long id;

    @Column(name = "standard_cabinet_drop_length_meters")
    private Double standardCabinetDropLengthMeters;

    @Column(name = "default_horizontal_clip_step")
    private Double defaultHorizontalClipStep;

    @Column(name = "default_vertical_clip_step")
    private Double defaultVerticalClipStep;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getStandardCabinetDropLengthMeters() {
        return standardCabinetDropLengthMeters;
    }

    public void setStandardCabinetDropLengthMeters(Double standardCabinetDropLengthMeters) {
        this.standardCabinetDropLengthMeters = standardCabinetDropLengthMeters;
    }

    public Double getDefaultHorizontalClipStep() {
        return defaultHorizontalClipStep;
    }

    public void setDefaultHorizontalClipStep(Double defaultHorizontalClipStep) {
        this.defaultHorizontalClipStep = defaultHorizontalClipStep;
    }

    public Double getDefaultVerticalClipStep() {
        return defaultVerticalClipStep;
    }

    public void setDefaultVerticalClipStep(Double defaultVerticalClipStep) {
        this.defaultVerticalClipStep = defaultVerticalClipStep;
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
}
