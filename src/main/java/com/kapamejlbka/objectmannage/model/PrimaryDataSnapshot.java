package com.kapamejlbka.objectmannage.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrimaryDataSnapshot {

    private List<DeviceGroup> deviceGroups = new ArrayList<>();
    private List<MountingRequirement> mountingElements = new ArrayList<>();
    private List<MaterialGroup> materialGroups = new ArrayList<>();
    private int totalConnectionPoints;

    public List<DeviceGroup> getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(List<DeviceGroup> deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

    public List<MountingRequirement> getMountingElements() {
        return mountingElements;
    }

    public void setMountingElements(List<MountingRequirement> mountingElements) {
        this.mountingElements = mountingElements;
    }

    public List<MaterialGroup> getMaterialGroups() {
        return materialGroups;
    }

    public void setMaterialGroups(List<MaterialGroup> materialGroups) {
        this.materialGroups = materialGroups;
    }

    public int getTotalConnectionPoints() {
        return totalConnectionPoints;
    }

    public void setTotalConnectionPoints(int totalConnectionPoints) {
        this.totalConnectionPoints = totalConnectionPoints;
    }

    public static class DeviceGroup {
        private UUID deviceTypeId;
        private String deviceTypeName;
        private int quantity;
        private String installLocation;
        private String connectionPoint;

        public UUID getDeviceTypeId() {
            return deviceTypeId;
        }

        public void setDeviceTypeId(UUID deviceTypeId) {
            this.deviceTypeId = deviceTypeId;
        }

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public void setDeviceTypeName(String deviceTypeName) {
            this.deviceTypeName = deviceTypeName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getInstallLocation() {
            return installLocation;
        }

        public void setInstallLocation(String installLocation) {
            this.installLocation = installLocation;
        }

        public String getConnectionPoint() {
            return connectionPoint;
        }

        public void setConnectionPoint(String connectionPoint) {
            this.connectionPoint = connectionPoint;
        }
    }

    public static class MountingRequirement {
        private UUID elementId;
        private String elementName;
        private String quantity;

        public UUID getElementId() {
            return elementId;
        }

        public void setElementId(UUID elementId) {
            this.elementId = elementId;
        }

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String elementName) {
            this.elementName = elementName;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
    }

    public static class MaterialGroup {
        private String groupName;
        private String surface;
        private List<MaterialUsage> materials = new ArrayList<>();

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getSurface() {
            return surface;
        }

        public void setSurface(String surface) {
            this.surface = surface;
        }

        public List<MaterialUsage> getMaterials() {
            return materials;
        }

        public void setMaterials(List<MaterialUsage> materials) {
            this.materials = materials;
        }
    }

    public static class MaterialUsage {
        private UUID materialId;
        private String materialName;
        private String amount;
        private String layingSurface;
        private String unit;

        public UUID getMaterialId() {
            return materialId;
        }

        public void setMaterialId(UUID materialId) {
            this.materialId = materialId;
        }

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getLayingSurface() {
            return layingSurface;
        }

        public void setLayingSurface(String layingSurface) {
            this.layingSurface = layingSurface;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
