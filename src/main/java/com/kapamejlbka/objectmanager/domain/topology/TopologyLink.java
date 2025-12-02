package com.kapamejlbka.objectmanager.domain.topology;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
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
@Table(name = "topology_links")
public class TopologyLink {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "calculation_id", nullable = false)
    private SystemCalculation calculation;

    @ManyToOne
    @JoinColumn(name = "from_node_id")
    private NetworkNode fromNode;

    @ManyToOne
    @JoinColumn(name = "to_node_id")
    private NetworkNode toNode;

    @ManyToOne
    @JoinColumn(name = "from_device_id")
    private EndpointDevice fromDevice;

    @ManyToOne
    @JoinColumn(name = "to_device_id")
    private EndpointDevice toDevice;

    @Column(name = "link_type", nullable = false)
    private String linkType;

    @Column(name = "cable_length")
    private Double cableLength;

    @Column(name = "is_wireless", nullable = false)
    private Boolean isWireless;

    @Column(name = "fiber_cores")
    private Integer fiberCores;

    @Column(name = "fiber_splice_count")
    private Integer fiberSpliceCount;

    @Column(name = "fiber_connector_count")
    private Integer fiberConnectorCount;

    @Column(name = "power_source_description")
    private String powerSourceDescription;

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
        if (isWireless == null) {
            isWireless = Boolean.FALSE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (isWireless == null) {
            isWireless = Boolean.FALSE;
        }
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

    public NetworkNode getFromNode() {
        return fromNode;
    }

    public void setFromNode(NetworkNode fromNode) {
        this.fromNode = fromNode;
    }

    public NetworkNode getToNode() {
        return toNode;
    }

    public void setToNode(NetworkNode toNode) {
        this.toNode = toNode;
    }

    public EndpointDevice getFromDevice() {
        return fromDevice;
    }

    public void setFromDevice(EndpointDevice fromDevice) {
        this.fromDevice = fromDevice;
    }

    public EndpointDevice getToDevice() {
        return toDevice;
    }

    public void setToDevice(EndpointDevice toDevice) {
        this.toDevice = toDevice;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public Double getCableLength() {
        return cableLength;
    }

    public void setCableLength(Double cableLength) {
        this.cableLength = cableLength;
    }

    public Boolean getWireless() {
        return isWireless;
    }

    public void setWireless(Boolean wireless) {
        isWireless = wireless;
    }

    public Integer getFiberCores() {
        return fiberCores;
    }

    public void setFiberCores(Integer fiberCores) {
        this.fiberCores = fiberCores;
    }

    public Integer getFiberSpliceCount() {
        return fiberSpliceCount;
    }

    public void setFiberSpliceCount(Integer fiberSpliceCount) {
        this.fiberSpliceCount = fiberSpliceCount;
    }

    public Integer getFiberConnectorCount() {
        return fiberConnectorCount;
    }

    public void setFiberConnectorCount(Integer fiberConnectorCount) {
        this.fiberConnectorCount = fiberConnectorCount;
    }

    public String getPowerSourceDescription() {
        return powerSourceDescription;
    }

    public void setPowerSourceDescription(String powerSourceDescription) {
        this.powerSourceDescription = powerSourceDescription;
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
