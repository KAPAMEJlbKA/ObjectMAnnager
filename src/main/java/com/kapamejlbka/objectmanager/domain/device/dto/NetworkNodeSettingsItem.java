package com.kapamejlbka.objectmanager.domain.device.dto;

public class NetworkNodeSettingsItem {

    private Long id;
    private String code;
    private String name;
    private String mountSurface;
    private Integer cabinetSize;
    private Boolean cabinetSizeAuto;
    private Integer baseCircuitBreakers;
    private Integer extraCircuitBreakers;
    private Integer baseSockets;
    private Integer extraSockets;
    private Integer incomingLinesCount;
    private Double powerLength;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getPowerLength() {
        return powerLength;
    }

    public void setPowerLength(Double powerLength) {
        this.powerLength = powerLength;
    }
}
