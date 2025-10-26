package com.kapamejlbka.objectmannage.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrimaryDataSummary {

    private final boolean hasData;
    private final boolean parseError;
    private final String errorMessage;
    private final List<DeviceTypeSummary> deviceTypeSummaries;
    private final List<CableLengthSummary> cableLengthSummaries;
    private final List<CableFunctionSummary> cableFunctionSummaries;
    private final List<NodeSummary> nodeSummaries;
    private final List<CameraDetail> cameraDetails;
    private final List<MaterialGroupSummary> materialGroupSummaries;
    private final List<AdditionalMaterialItem> additionalMaterials;
    private final List<MaterialTotal> materialTotals;
    private final List<MaterialTotal> mountingElementTotals;
    private final String overallMaterialSummary;
    private final int totalDeviceCount;
    private final int totalNodes;
    private final int unnamedConnectionAssignments;
    private final Integer declaredConnectionAssignments;
    private final double totalCableLength;
    private final String deviceTypeBreakdown;

    private PrimaryDataSummary(Builder builder) {
        this.hasData = builder.hasData;
        this.parseError = builder.parseError;
        this.errorMessage = builder.errorMessage;
        this.deviceTypeSummaries = Collections.unmodifiableList(new ArrayList<>(builder.deviceTypeSummaries));
        this.cableLengthSummaries = Collections.unmodifiableList(new ArrayList<>(builder.cableLengthSummaries));
        this.cableFunctionSummaries = Collections.unmodifiableList(new ArrayList<>(builder.cableFunctionSummaries));
        this.nodeSummaries = Collections.unmodifiableList(new ArrayList<>(builder.nodeSummaries));
        this.cameraDetails = Collections.unmodifiableList(new ArrayList<>(builder.cameraDetails));
        this.materialGroupSummaries = Collections.unmodifiableList(new ArrayList<>(builder.materialGroupSummaries));
        this.additionalMaterials = Collections.unmodifiableList(new ArrayList<>(builder.additionalMaterials));
        this.materialTotals = Collections.unmodifiableList(new ArrayList<>(builder.materialTotals));
        this.mountingElementTotals = Collections.unmodifiableList(new ArrayList<>(builder.mountingElementTotals));
        this.overallMaterialSummary = builder.overallMaterialSummary;
        this.totalDeviceCount = builder.totalDeviceCount;
        this.totalNodes = builder.totalNodes;
        this.unnamedConnectionAssignments = builder.unnamedConnectionAssignments;
        this.declaredConnectionAssignments = builder.declaredConnectionAssignments;
        this.totalCableLength = builder.totalCableLength;
        this.deviceTypeBreakdown = builder.deviceTypeBreakdown;
    }

    public static PrimaryDataSummary empty() {
        return new Builder().withHasData(false).build();
    }

    public static PrimaryDataSummary parseError(String message) {
        return new Builder()
                .withParseError(true)
                .withErrorMessage(message)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isHasData() {
        return hasData;
    }

    public boolean isParseError() {
        return parseError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<DeviceTypeSummary> getDeviceTypeSummaries() {
        return deviceTypeSummaries;
    }

    public List<CableLengthSummary> getCableLengthSummaries() {
        return cableLengthSummaries;
    }

    public List<CableFunctionSummary> getCableFunctionSummaries() {
        return cableFunctionSummaries;
    }

    public List<CameraDetail> getCameraDetails() {
        return cameraDetails;
    }

    public List<AdditionalMaterialItem> getAdditionalMaterials() {
        return additionalMaterials;
    }

    public List<MaterialGroupSummary> getMaterialGroupSummaries() {
        return materialGroupSummaries;
    }

    public List<NodeSummary> getNodeSummaries() {
        return nodeSummaries;
    }

    public List<MaterialTotal> getMaterialTotals() {
        return materialTotals;
    }

    public List<MaterialTotal> getMountingElementTotals() {
        return mountingElementTotals;
    }

    public String getOverallMaterialSummary() {
        return overallMaterialSummary;
    }

    public int getTotalDeviceCount() {
        return totalDeviceCount;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getUnnamedConnectionAssignments() {
        return unnamedConnectionAssignments;
    }

    public Integer getDeclaredConnectionAssignments() {
        return declaredConnectionAssignments;
    }

    public double getTotalCableLength() {
        return totalCableLength;
    }

    public String getDeviceTypeBreakdown() {
        return deviceTypeBreakdown;
    }

    public static class Builder {
        private boolean hasData = true;
        private boolean parseError;
        private String errorMessage;
        private final List<DeviceTypeSummary> deviceTypeSummaries = new ArrayList<>();
        private final List<CableLengthSummary> cableLengthSummaries = new ArrayList<>();
        private final List<CableFunctionSummary> cableFunctionSummaries = new ArrayList<>();
        private final List<NodeSummary> nodeSummaries = new ArrayList<>();
        private final List<MaterialGroupSummary> materialGroupSummaries = new ArrayList<>();
        private final List<CameraDetail> cameraDetails = new ArrayList<>();
        private final List<AdditionalMaterialItem> additionalMaterials = new ArrayList<>();
        private final List<MaterialTotal> materialTotals = new ArrayList<>();
        private final List<MaterialTotal> mountingElementTotals = new ArrayList<>();
        private String overallMaterialSummary;
        private int totalDeviceCount;
        private int totalNodes;
        private int unnamedConnectionAssignments;
        private Integer declaredConnectionAssignments;
        private double totalCableLength;
        private String deviceTypeBreakdown;

        public Builder withHasData(boolean hasData) {
            this.hasData = hasData;
            return this;
        }

        public Builder withParseError(boolean parseError) {
            this.parseError = parseError;
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder addDeviceTypeSummary(DeviceTypeSummary summary) {
            if (summary != null) {
                this.deviceTypeSummaries.add(summary);
            }
            return this;
        }

        public Builder addCableLengthSummary(CableLengthSummary summary) {
            if (summary != null) {
                this.cableLengthSummaries.add(summary);
            }
            return this;
        }

        public Builder addCableFunctionSummary(CableFunctionSummary summary) {
            if (summary != null) {
                this.cableFunctionSummaries.add(summary);
            }
            return this;
        }

        public Builder addNodeSummary(NodeSummary summary) {
            if (summary != null) {
                this.nodeSummaries.add(summary);
            }
            return this;
        }

        public Builder addCameraDetail(CameraDetail detail) {
            if (detail != null) {
                this.cameraDetails.add(detail);
            }
            return this;
        }

        public Builder addMaterialGroupSummary(MaterialGroupSummary summary) {
            if (summary != null) {
                this.materialGroupSummaries.add(summary);
            }
            return this;
        }

        public Builder addMaterialTotal(MaterialTotal total) {
            if (total != null) {
                this.materialTotals.add(total);
            }
            return this;
        }

        public Builder addMountingElementTotal(MaterialTotal total) {
            if (total != null) {
                this.mountingElementTotals.add(total);
            }
            return this;
        }

        public Builder withOverallMaterialSummary(String summary) {
            this.overallMaterialSummary = summary;
            return this;
        }

        public Builder withTotalDeviceCount(int totalDeviceCount) {
            this.totalDeviceCount = totalDeviceCount;
            return this;
        }

        public Builder withTotalNodes(int totalNodes) {
            this.totalNodes = totalNodes;
            return this;
        }

        public Builder withUnnamedConnectionAssignments(int unnamedConnectionAssignments) {
            this.unnamedConnectionAssignments = unnamedConnectionAssignments;
            return this;
        }

        public Builder withDeclaredConnectionAssignments(Integer declaredConnectionAssignments) {
            this.declaredConnectionAssignments = declaredConnectionAssignments;
            return this;
        }

        public Builder withTotalCableLength(double totalCableLength) {
            this.totalCableLength = totalCableLength;
            return this;
        }

        public Builder withDeviceTypeBreakdown(String deviceTypeBreakdown) {
            this.deviceTypeBreakdown = deviceTypeBreakdown;
            return this;
        }

        public Builder addAdditionalMaterial(AdditionalMaterialItem item) {
            if (item != null) {
                this.additionalMaterials.add(item);
            }
            return this;
        }

        public PrimaryDataSummary build() {
            if (!hasData) {
                this.deviceTypeSummaries.clear();
                this.cableLengthSummaries.clear();
                this.cableFunctionSummaries.clear();
                this.nodeSummaries.clear();
                this.materialGroupSummaries.clear();
                this.cameraDetails.clear();
                this.additionalMaterials.clear();
                this.materialTotals.clear();
                this.mountingElementTotals.clear();
                this.totalDeviceCount = 0;
                this.totalNodes = 0;
                this.unnamedConnectionAssignments = 0;
                this.declaredConnectionAssignments = null;
                this.totalCableLength = 0.0;
                this.deviceTypeBreakdown = null;
            }
            return new PrimaryDataSummary(this);
        }
    }

    public static class DeviceTypeSummary {
        private final String deviceTypeName;
        private final int quantity;

        public DeviceTypeSummary(String deviceTypeName, int quantity) {
            this.deviceTypeName = deviceTypeName;
            this.quantity = quantity;
        }

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class CableFunctionSummary {
        private final String functionName;
        private final double totalLength;

        public CableFunctionSummary(String functionName, double totalLength) {
            this.functionName = functionName;
            this.totalLength = totalLength;
        }

        public String getFunctionName() {
            return functionName;
        }

        public double getTotalLength() {
            return totalLength;
        }
    }

    public static class CableLengthSummary {
        private final String cableTypeName;
        private final double totalLength;
        private final boolean classificationMissing;

        public CableLengthSummary(String cableTypeName, double totalLength, boolean classificationMissing) {
            this.cableTypeName = cableTypeName;
            this.totalLength = totalLength;
            this.classificationMissing = classificationMissing;
        }

        public String getCableTypeName() {
            return cableTypeName;
        }

        public double getTotalLength() {
            return totalLength;
        }

        public boolean isClassificationMissing() {
            return classificationMissing;
        }
    }

    public static class NodeSummary {
        private final String name;
        private final String mountingElementName;
        private final Double distanceToPower;
        private final String powerCableTypeName;
        private final String layingMaterialName;
        private final String layingMaterialUnit;
        private final String layingSurface;
        private final String layingSurfaceCategory;
        private final int singleSocketCount;
        private final int doubleSocketCount;
        private final int breakerCount;
        private final int breakerBoxCount;
        private final int nshviCount;
        private final List<NodeMaterialGroupSummary> materialGroups;
        private final List<MaterialTotal> materialTotals;

        public NodeSummary(String name,
                           String mountingElementName,
                           Double distanceToPower,
                           String powerCableTypeName,
                           String layingMaterialName,
                           String layingMaterialUnit,
                           String layingSurface,
                           String layingSurfaceCategory,
                           int singleSocketCount,
                           int doubleSocketCount,
                           int breakerCount,
                           int breakerBoxCount,
                           int nshviCount,
                           List<NodeMaterialGroupSummary> materialGroups,
                           List<MaterialTotal> materialTotals) {
            this.name = name;
            this.mountingElementName = mountingElementName;
            this.distanceToPower = distanceToPower;
            this.powerCableTypeName = powerCableTypeName;
            this.layingMaterialName = layingMaterialName;
            this.layingMaterialUnit = layingMaterialUnit;
            this.layingSurface = layingSurface;
            this.layingSurfaceCategory = layingSurfaceCategory;
            this.singleSocketCount = singleSocketCount;
            this.doubleSocketCount = doubleSocketCount;
            this.breakerCount = breakerCount;
            this.breakerBoxCount = breakerBoxCount;
            this.nshviCount = nshviCount;
            if (materialGroups == null || materialGroups.isEmpty()) {
                this.materialGroups = Collections.emptyList();
            } else {
                this.materialGroups = Collections.unmodifiableList(new ArrayList<>(materialGroups));
            }
            if (materialTotals == null || materialTotals.isEmpty()) {
                this.materialTotals = Collections.emptyList();
            } else {
                this.materialTotals = Collections.unmodifiableList(new ArrayList<>(materialTotals));
            }
        }

        public String getName() {
            return name;
        }

        public String getMountingElementName() {
            return mountingElementName;
        }

        public Double getDistanceToPower() {
            return distanceToPower;
        }

        public String getPowerCableTypeName() {
            return powerCableTypeName;
        }

        public String getLayingMaterialName() {
            return layingMaterialName;
        }

        public String getLayingMaterialUnit() {
            return layingMaterialUnit;
        }

        public String getLayingSurface() {
            return layingSurface;
        }

        public String getLayingSurfaceCategory() {
            return layingSurfaceCategory;
        }

        public int getSingleSocketCount() {
            return singleSocketCount;
        }

        public int getDoubleSocketCount() {
            return doubleSocketCount;
        }

        public int getBreakerCount() {
            return breakerCount;
        }

        public int getBreakerBoxCount() {
            return breakerBoxCount;
        }

        public int getNshviCount() {
            return nshviCount;
        }

        public List<NodeMaterialGroupSummary> getMaterialGroups() {
            return materialGroups;
        }

        public List<MaterialTotal> getMaterialTotals() {
            return materialTotals;
        }

    }

    public static class NodeMaterialGroupSummary {
        private final String label;
        private final List<MaterialUsageSummary> materials;

        public NodeMaterialGroupSummary(String label, List<MaterialUsageSummary> materials) {
            this.label = label;
            if (materials == null || materials.isEmpty()) {
                this.materials = Collections.emptyList();
            } else {
                this.materials = Collections.unmodifiableList(new ArrayList<>(materials));
            }
        }

        public String getLabel() {
            return label;
        }

        public List<MaterialUsageSummary> getMaterials() {
            return materials;
        }
    }

    public static class CameraDetail {
        private final String deviceTypeName;
        private final int quantity;
        private final String installLocation;
        private final String connectionPoint;
        private final String surfaceLabel;
        private final String accessoryLabel;
        private final Double viewingDepth;

        public CameraDetail(String deviceTypeName,
                            int quantity,
                            String installLocation,
                            String connectionPoint,
                            String surfaceLabel,
                            String accessoryLabel,
                            Double viewingDepth) {
            this.deviceTypeName = deviceTypeName;
            this.quantity = quantity;
            this.installLocation = installLocation;
            this.connectionPoint = connectionPoint;
            this.surfaceLabel = surfaceLabel;
            this.accessoryLabel = accessoryLabel;
            this.viewingDepth = viewingDepth;
        }

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getInstallLocation() {
            return installLocation;
        }

        public String getConnectionPoint() {
            return connectionPoint;
        }

        public String getSurfaceLabel() {
            return surfaceLabel;
        }

        public String getAccessoryLabel() {
            return accessoryLabel;
        }

        public Double getViewingDepth() {
            return viewingDepth;
        }
    }

    public static class AdditionalMaterialItem {
        private final String name;
        private final String unit;
        private final double quantity;

        public AdditionalMaterialItem(String name, String unit, double quantity) {
            this.name = name;
            this.unit = unit;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public String getUnit() {
            return unit;
        }

        public double getQuantity() {
            return quantity;
        }
    }

    public static class MaterialGroupSummary {
        private final String label;
        private final String surfaceLabel;
        private final List<MaterialUsageSummary> materials;

        public MaterialGroupSummary(String label, String surfaceLabel, List<MaterialUsageSummary> materials) {
            this.label = label;
            this.surfaceLabel = surfaceLabel;
            if (materials == null || materials.isEmpty()) {
                this.materials = Collections.emptyList();
            } else {
                this.materials = Collections.unmodifiableList(new ArrayList<>(materials));
            }
        }

        public String getLabel() {
            return label;
        }

        public String getSurfaceLabel() {
            return surfaceLabel;
        }

        public List<MaterialUsageSummary> getMaterials() {
            return materials;
        }
    }

    public static class MaterialUsageSummary {
        private final String materialName;
        private final String amountWithUnit;
        private final String surfaceLabel;

        public MaterialUsageSummary(String materialName, String amountWithUnit, String surfaceLabel) {
            this.materialName = materialName;
            this.amountWithUnit = amountWithUnit;
            this.surfaceLabel = surfaceLabel;
        }

        public String getMaterialName() {
            return materialName;
        }

        public String getAmountWithUnit() {
            return amountWithUnit;
        }

        public String getSurfaceLabel() {
            return surfaceLabel;
        }
    }

    public static class MaterialTotal {
        private final String name;
        private final String unit;
        private final double quantity;

        public MaterialTotal(String name, String unit, double quantity) {
            this.name = name;
            this.unit = unit;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public String getUnit() {
            return unit;
        }

        public double getQuantity() {
            return quantity;
        }
    }
}
