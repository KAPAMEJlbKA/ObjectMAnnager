package com.kapamejlbka.objectmannage.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrimaryDataSnapshot {

    private List<DeviceGroup> deviceGroups = new ArrayList<>();
    private List<ConnectionPoint> connectionPoints = new ArrayList<>();
    private List<MountingRequirement> mountingElements = new ArrayList<>();
    private List<MaterialGroup> materialGroups = new ArrayList<>();
    private int totalConnectionPoints;

    public List<DeviceGroup> getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(List<DeviceGroup> deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

    public List<ConnectionPoint> getConnectionPoints() {
        return connectionPoints;
    }

    public void setConnectionPoints(List<ConnectionPoint> connectionPoints) {
        this.connectionPoints = connectionPoints;
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
        private Double distanceToConnectionPoint;
        private String groupLabel;
        private String installSurfaceCategory;
        private String cameraAccessory;
        private Double cameraViewingDepth;
        private UUID signalCableTypeId;
        private String signalCableTypeName;
        private UUID lowVoltageCableTypeId;
        private String lowVoltageCableTypeName;

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

        public String getInstallSurfaceCategory() {
            return installSurfaceCategory;
        }

        public void setInstallSurfaceCategory(String installSurfaceCategory) {
            this.installSurfaceCategory = installSurfaceCategory;
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

        public String getSignalCableTypeName() {
            return signalCableTypeName;
        }

        public void setSignalCableTypeName(String signalCableTypeName) {
            this.signalCableTypeName = signalCableTypeName;
        }

        public UUID getLowVoltageCableTypeId() {
            return lowVoltageCableTypeId;
        }

        public void setLowVoltageCableTypeId(UUID lowVoltageCableTypeId) {
            this.lowVoltageCableTypeId = lowVoltageCableTypeId;
        }

        public String getLowVoltageCableTypeName() {
            return lowVoltageCableTypeName;
        }

        public void setLowVoltageCableTypeName(String lowVoltageCableTypeName) {
            this.lowVoltageCableTypeName = lowVoltageCableTypeName;
        }

    }

    public static class ConnectionPoint {
        private String name;
        private UUID mountingElementId;
        private String mountingElementName;
        private Double distanceToPower;
        private UUID powerCableTypeId;
        private String powerCableTypeName;
        private UUID layingMaterialId;
        private String layingMaterialName;
        private String layingMaterialUnit;
        private String layingSurface;
        private String layingSurfaceCategory;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UUID getMountingElementId() {
            return mountingElementId;
        }

        public void setMountingElementId(UUID mountingElementId) {
            this.mountingElementId = mountingElementId;
        }

        public String getMountingElementName() {
            return mountingElementName;
        }

        public void setMountingElementName(String mountingElementName) {
            this.mountingElementName = mountingElementName;
        }

        public Double getDistanceToPower() {
            return distanceToPower;
        }

        public void setDistanceToPower(Double distanceToPower) {
            this.distanceToPower = distanceToPower;
        }

        public UUID getPowerCableTypeId() {
            return powerCableTypeId;
        }

        public void setPowerCableTypeId(UUID powerCableTypeId) {
            this.powerCableTypeId = powerCableTypeId;
        }

        public String getPowerCableTypeName() {
            return powerCableTypeName;
        }

        public void setPowerCableTypeName(String powerCableTypeName) {
            this.powerCableTypeName = powerCableTypeName;
        }

        public UUID getLayingMaterialId() {
            return layingMaterialId;
        }

        public void setLayingMaterialId(UUID layingMaterialId) {
            this.layingMaterialId = layingMaterialId;
        }

        public String getLayingMaterialName() {
            return layingMaterialName;
        }

        public void setLayingMaterialName(String layingMaterialName) {
            this.layingMaterialName = layingMaterialName;
        }

        public String getLayingMaterialUnit() {
            return layingMaterialUnit;
        }

        public void setLayingMaterialUnit(String layingMaterialUnit) {
            this.layingMaterialUnit = layingMaterialUnit;
        }

        public String getLayingSurface() {
            return layingSurface;
        }

        public void setLayingSurface(String layingSurface) {
            this.layingSurface = layingSurface;
        }

        public String getLayingSurfaceCategory() {
            return layingSurfaceCategory;
        }

        public void setLayingSurfaceCategory(String layingSurfaceCategory) {
            this.layingSurfaceCategory = layingSurfaceCategory;
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
        private String groupLabel;
        private String surface;
        private String surfaceCategory;
        private List<MaterialUsage> materials = new ArrayList<>();

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupLabel() {
            return groupLabel;
        }

        public void setGroupLabel(String groupLabel) {
            this.groupLabel = groupLabel;
        }

        public String getSurface() {
            return surface;
        }

        public void setSurface(String surface) {
            this.surface = surface;
        }

        public String getSurfaceCategory() {
            return surfaceCategory;
        }

        public void setSurfaceCategory(String surfaceCategory) {
            this.surfaceCategory = surfaceCategory;
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
        private String layingSurfaceCategory;
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

        public String getLayingSurfaceCategory() {
            return layingSurfaceCategory;
        }

        public void setLayingSurfaceCategory(String layingSurfaceCategory) {
            this.layingSurfaceCategory = layingSurfaceCategory;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
