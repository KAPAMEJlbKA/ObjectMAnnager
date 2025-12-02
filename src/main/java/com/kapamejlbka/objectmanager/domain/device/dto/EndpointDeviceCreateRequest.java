package com.kapamejlbka.objectmanager.domain.device.dto;

public class EndpointDeviceCreateRequest {

    private String type;
    private String code;
    private String name;
    private String locationDescription;
    private Double viewDepth;
    private String mountSurface;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public Double getViewDepth() {
        return viewDepth;
    }

    public void setViewDepth(Double viewDepth) {
        this.viewDepth = viewDepth;
    }

    public String getMountSurface() {
        return mountSurface;
    }

    public void setMountSurface(String mountSurface) {
        this.mountSurface = mountSurface;
    }
}
