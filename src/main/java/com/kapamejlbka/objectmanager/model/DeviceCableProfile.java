package com.kapamejlbka.objectmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "device_cable_profiles")
public class DeviceCableProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DeviceType deviceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cable_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CableType cableType;

    @Column(nullable = false)
    private String endpointName;

    protected DeviceCableProfile() {
    }

    public DeviceCableProfile(DeviceType deviceType, CableType cableType, String endpointName) {
        this.deviceType = deviceType;
        this.cableType = cableType;
        this.endpointName = endpointName;
    }

    public UUID getId() {
        return id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public CableType getCableType() {
        return cableType;
    }

    public void setCableType(CableType cableType) {
        this.cableType = cableType;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
}
