package com.kapamejlbka.objectmanager.domain.device;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
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
@Table(name = "network_nodes")
public class NetworkNode {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "calculation_id", nullable = false)
    private SystemCalculation calculation;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "mount_surface")
    private String mountSurface;

    @Column(name = "cabinet_size")
    private Integer cabinetSize;

    @Column(name = "cabinet_size_auto", nullable = false)
    private Boolean cabinetSizeAuto;

    @Column(name = "base_circuit_breakers", nullable = false)
    private Integer baseCircuitBreakers;

    @Column(name = "extra_circuit_breakers", nullable = false)
    private Integer extraCircuitBreakers;

    @Column(name = "base_sockets", nullable = false)
    private Integer baseSockets;

    @Column(name = "extra_sockets", nullable = false)
    private Integer extraSockets;

    @Column(name = "incoming_lines_count")
    private Integer incomingLinesCount;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMountSurface() {
        return mountSurface;
    }

    public void setMountSurface(String mountSurface) {
        this.mountSurface = mountSurface;
    }

    public Integer getCabinetSize() {
        return cabinetSize;
    }

    public void setCabinetSize(Integer cabinetSize) {
        this.cabinetSize = cabinetSize;
    }

    public Boolean getCabinetSizeAuto() {
        return cabinetSizeAuto;
    }

    public void setCabinetSizeAuto(Boolean cabinetSizeAuto) {
        this.cabinetSizeAuto = cabinetSizeAuto;
    }

    public Integer getBaseCircuitBreakers() {
        return baseCircuitBreakers;
    }

    public void setBaseCircuitBreakers(Integer baseCircuitBreakers) {
        this.baseCircuitBreakers = baseCircuitBreakers;
    }

    public Integer getExtraCircuitBreakers() {
        return extraCircuitBreakers;
    }

    public void setExtraCircuitBreakers(Integer extraCircuitBreakers) {
        this.extraCircuitBreakers = extraCircuitBreakers;
    }

    public Integer getBaseSockets() {
        return baseSockets;
    }

    public void setBaseSockets(Integer baseSockets) {
        this.baseSockets = baseSockets;
    }

    public Integer getExtraSockets() {
        return extraSockets;
    }

    public void setExtraSockets(Integer extraSockets) {
        this.extraSockets = extraSockets;
    }

    public Integer getIncomingLinesCount() {
        return incomingLinesCount;
    }

    public void setIncomingLinesCount(Integer incomingLinesCount) {
        this.incomingLinesCount = incomingLinesCount;
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
