package com.kapamejlbka.objectmanager.model.primarydata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrimaryDataV2 {

    private int schemaVersion = 2;
    private Integer totalDeviceCount;
    private Integer totalNodeCount;
    private Integer workspaceCount;
    private Integer totalConnectionPoints;
    private String nodeConnectionMethod;
    private String nodeConnectionDiagram;
    private String mainWorkspaceLocation;
    private List<LocationDTO> locations = new ArrayList<>();
    private List<NodeDTO> nodes = new ArrayList<>();
    private List<DeviceDTO> devices = new ArrayList<>();
    private List<WorkspaceDTO> workspaces = new ArrayList<>();
    private List<MaterialUsageDTO> materials = new ArrayList<>();

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Integer getTotalDeviceCount() {
        return totalDeviceCount;
    }

    public void setTotalDeviceCount(Integer totalDeviceCount) {
        this.totalDeviceCount = totalDeviceCount;
    }

    public Integer getTotalNodeCount() {
        return totalNodeCount;
    }

    public void setTotalNodeCount(Integer totalNodeCount) {
        this.totalNodeCount = totalNodeCount;
    }

    public Integer getWorkspaceCount() {
        return workspaceCount;
    }

    public void setWorkspaceCount(Integer workspaceCount) {
        this.workspaceCount = workspaceCount;
    }

    public Integer getTotalConnectionPoints() {
        return totalConnectionPoints;
    }

    public void setTotalConnectionPoints(Integer totalConnectionPoints) {
        this.totalConnectionPoints = totalConnectionPoints;
    }

    public String getNodeConnectionMethod() {
        return nodeConnectionMethod;
    }

    public void setNodeConnectionMethod(String nodeConnectionMethod) {
        this.nodeConnectionMethod = nodeConnectionMethod;
    }

    public String getNodeConnectionDiagram() {
        return nodeConnectionDiagram;
    }

    public void setNodeConnectionDiagram(String nodeConnectionDiagram) {
        this.nodeConnectionDiagram = nodeConnectionDiagram;
    }

    public String getMainWorkspaceLocation() {
        return mainWorkspaceLocation;
    }

    public void setMainWorkspaceLocation(String mainWorkspaceLocation) {
        this.mainWorkspaceLocation = mainWorkspaceLocation;
    }

    public List<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationDTO> locations) {
        this.locations = locations == null ? new ArrayList<>() : locations;
    }

    public List<NodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDTO> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<DeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDTO> devices) {
        this.devices = devices == null ? new ArrayList<>() : devices;
    }

    public List<WorkspaceDTO> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<WorkspaceDTO> workspaces) {
        this.workspaces = workspaces == null ? new ArrayList<>() : workspaces;
    }

    public List<MaterialUsageDTO> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialUsageDTO> materials) {
        this.materials = materials == null ? new ArrayList<>() : materials;
    }

    public static class LocationDTO {
        private UUID id;
        private String label;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class NodeDTO {
        private UUID id;
        private String name;
        private UUID mountingElementId;
        private Double distanceToPower;
        private UUID powerCableTypeId;
        private UUID layingMaterialId;
        private String layingSurface;
        private String layingSurfaceCategory;
        private Integer singleSocketCount;
        private Integer doubleSocketCount;
        private Integer breakerCount;
        private Integer breakerBoxCount;
        private Integer nshviCount;
        private UUID locationId;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

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

        public UUID getLayingMaterialId() {
            return layingMaterialId;
        }

        public void setLayingMaterialId(UUID layingMaterialId) {
            this.layingMaterialId = layingMaterialId;
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

        public Integer getSingleSocketCount() {
            return singleSocketCount;
        }

        public void setSingleSocketCount(Integer singleSocketCount) {
            this.singleSocketCount = singleSocketCount;
        }

        public Integer getDoubleSocketCount() {
            return doubleSocketCount;
        }

        public void setDoubleSocketCount(Integer doubleSocketCount) {
            this.doubleSocketCount = doubleSocketCount;
        }

        public Integer getBreakerCount() {
            return breakerCount;
        }

        public void setBreakerCount(Integer breakerCount) {
            this.breakerCount = breakerCount;
        }

        public Integer getBreakerBoxCount() {
            return breakerBoxCount;
        }

        public void setBreakerBoxCount(Integer breakerBoxCount) {
            this.breakerBoxCount = breakerBoxCount;
        }

        public Integer getNshviCount() {
            return nshviCount;
        }

        public void setNshviCount(Integer nshviCount) {
            this.nshviCount = nshviCount;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public void setLocationId(UUID locationId) {
            this.locationId = locationId;
        }
    }

    public static class DeviceDTO {
        private UUID id;
        private UUID deviceTypeId;
        private UUID locationId;
        private UUID nodeId;
        private Integer quantity;
        private String installLocation;
        private String groupLabel;
        private UUID signalCableTypeId;
        private UUID lowVoltageCableTypeId;
        private Double distanceToNode;
        private String installSurfaceCategory;
        private String cameraAccessory;
        private Double cameraViewingDepth;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getDeviceTypeId() {
            return deviceTypeId;
        }

        public void setDeviceTypeId(UUID deviceTypeId) {
            this.deviceTypeId = deviceTypeId;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public void setLocationId(UUID locationId) {
            this.locationId = locationId;
        }

        public UUID getNodeId() {
            return nodeId;
        }

        public void setNodeId(UUID nodeId) {
            this.nodeId = nodeId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getInstallLocation() {
            return installLocation;
        }

        public void setInstallLocation(String installLocation) {
            this.installLocation = installLocation;
        }

        public String getGroupLabel() {
            return groupLabel;
        }

        public void setGroupLabel(String groupLabel) {
            this.groupLabel = groupLabel;
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

        public Double getDistanceToNode() {
            return distanceToNode;
        }

        public void setDistanceToNode(Double distanceToNode) {
            this.distanceToNode = distanceToNode;
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
    }

    public static class WorkspaceDTO {
        private String name;
        private String equipment;
        private UUID nodeId;
        private String locationLabel;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEquipment() {
            return equipment;
        }

        public void setEquipment(String equipment) {
            this.equipment = equipment;
        }

        public UUID getNodeId() {
            return nodeId;
        }

        public void setNodeId(UUID nodeId) {
            this.nodeId = nodeId;
        }

        public String getLocationLabel() {
            return locationLabel;
        }

        public void setLocationLabel(String locationLabel) {
            this.locationLabel = locationLabel;
        }
    }

    public static class MaterialUsageDTO {
        private UUID materialId;
        private String amount;
        private String unit;
        private String layingSurface;
        private String layingSurfaceCategory;
        private String groupLabel;

        public UUID getMaterialId() {
            return materialId;
        }

        public void setMaterialId(UUID materialId) {
            this.materialId = materialId;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
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

        public String getGroupLabel() {
            return groupLabel;
        }

        public void setGroupLabel(String groupLabel) {
            this.groupLabel = groupLabel;
        }
    }
}
