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

    public List<NodeSummary> getNodeSummaries() {
        return nodeSummaries;
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

        public PrimaryDataSummary build() {
            if (!hasData) {
                this.deviceTypeSummaries.clear();
                this.cableLengthSummaries.clear();
                this.cableFunctionSummaries.clear();
                this.nodeSummaries.clear();
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
        private final String layingMethod;

        public NodeSummary(String name,
                           String mountingElementName,
                           Double distanceToPower,
                           String powerCableTypeName,
                           String layingMethod) {
            this.name = name;
            this.mountingElementName = mountingElementName;
            this.distanceToPower = distanceToPower;
            this.powerCableTypeName = powerCableTypeName;
            this.layingMethod = layingMethod;
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

        public String getLayingMethod() {
            return layingMethod;
        }
    }
}
