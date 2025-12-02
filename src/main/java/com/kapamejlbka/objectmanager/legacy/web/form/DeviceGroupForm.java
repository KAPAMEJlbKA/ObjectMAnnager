package com.kapamejlbka.objectmanager.legacy.web.form;

import java.util.UUID;
import org.springframework.util.StringUtils;

public class DeviceGroupForm {

    private UUID deviceTypeId;
    private Integer deviceCount;
    private String installLocation;
    private String installSurfaceCategory;
    private String connectionPoint;
    private Double distanceToConnectionPoint;
    private String groupLabel;
    private String cameraAccessory;
    private Double cameraViewingDepth;
    private UUID signalCableTypeId;
    private UUID lowVoltageCableTypeId;

    public boolean isEmpty() {
        return (deviceTypeId == null)
                && (deviceCount == null || deviceCount == 0)
                && !StringUtils.hasText(installLocation)
                && !StringUtils.hasText(installSurfaceCategory)
                && !StringUtils.hasText(connectionPoint)
                && distanceToConnectionPoint == null
                && !StringUtils.hasText(groupLabel)
                && !StringUtils.hasText(cameraAccessory)
                && cameraViewingDepth == null
                && signalCableTypeId == null
                && lowVoltageCableTypeId == null;
    }

    public UUID getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(UUID deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Integer getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(Integer deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public String getInstallSurfaceCategory() {
        return installSurfaceCategory;
    }

    public void setInstallSurfaceCategory(String installSurfaceCategory) {
        this.installSurfaceCategory = installSurfaceCategory;
    }

    public String getConnectionPoint() {
        return connectionPoint;
    }

    public void setConnectionPoint(String connectionPoint) {
        this.connectionPoint = connectionPoint;
    }

    public Double getDistanceToConnectionPoint() {
        return distanceToConnectionPoint;
    }

    public void setDistanceToConnectionPoint(Double distanceToConnectionPoint) {
        this.distanceToConnectionPoint = distanceToConnectionPoint;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public String getCameraAccessory() {
        return cameraAccessory;
    }

    public void setCameraAccessory(String cameraAccessory) {
        this.cameraAccessory = cameraAccessory;
    }

    public Double getCameraViewingDepth() {
        return cameraViewingDepth;
    }

    public void setCameraViewingDepth(Double cameraViewingDepth) {
        this.cameraViewingDepth = cameraViewingDepth;
    }

    public UUID getSignalCableTypeId() {
        return signalCableTypeId;
    }

    public void setSignalCableTypeId(UUID signalCableTypeId) {
        this.signalCableTypeId = signalCableTypeId;
    }

    public UUID getLowVoltageCableTypeId() {
        return lowVoltageCableTypeId;
    }

    public void setLowVoltageCableTypeId(UUID lowVoltageCableTypeId) {
        this.lowVoltageCableTypeId = lowVoltageCableTypeId;
    }
}
