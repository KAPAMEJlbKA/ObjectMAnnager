package com.kapamejlbka.objectmanager.domain.topology;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.material.Material;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "installation_routes")
public class InstallationRoute {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "calculation_id", nullable = false)
    private SystemCalculation calculation;

    @Column(nullable = false)
    private String name;

    @Column(name = "route_type", nullable = false)
    private String routeType;

    @Column(name = "mount_surface")
    private String mountSurface;

    @ManyToOne
    @JoinColumn(name = "main_material_id")
    private Material mainMaterial;

    @Column(name = "length_meters", nullable = false)
    private Double lengthMeters;

    @Column(name = "orientation")
    private String orientation;

    @Column(name = "fixing_method")
    private String fixingMethod;

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

    public SystemCalculation getCalculation() {
        return calculation;
    }

    public void setCalculation(SystemCalculation calculation) {
        this.calculation = calculation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getMountSurface() {
        return mountSurface;
    }

    public void setMountSurface(String mountSurface) {
        this.mountSurface = mountSurface;
    }

    public Material getMainMaterial() {
        return mainMaterial;
    }

    public void setMainMaterial(Material mainMaterial) {
        this.mainMaterial = mainMaterial;
    }

    public Double getLengthMeters() {
        return lengthMeters;
    }

    public void setLengthMeters(Double lengthMeters) {
        this.lengthMeters = lengthMeters;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getFixingMethod() {
        return fixingMethod;
    }

    public void setFixingMethod(String fixingMethod) {
        this.fixingMethod = fixingMethod;
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
