package com.kapamejlbka.objectmanager.domain.topology.dto;

public class TopologyLinkUpdateRequest {

    private Long fromNodeId;
    private Long toNodeId;
    private Long fromDeviceId;
    private Long toDeviceId;
    private String linkType;
    private Double cableLength;
    private Boolean isWireless;
    private Integer fiberCores;
    private Integer fiberSpliceCount;
    private Integer fiberConnectorCount;
    private String powerSourceDescription;

    public Long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId) {
        this.toNodeId = toNodeId;
    }

    public Long getFromDeviceId() {
        return fromDeviceId;
    }

    public void setFromDeviceId(Long fromDeviceId) {
        this.fromDeviceId = fromDeviceId;
    }

    public Long getToDeviceId() {
        return toDeviceId;
    }

    public void setToDeviceId(Long toDeviceId) {
        this.toDeviceId = toDeviceId;
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
}
