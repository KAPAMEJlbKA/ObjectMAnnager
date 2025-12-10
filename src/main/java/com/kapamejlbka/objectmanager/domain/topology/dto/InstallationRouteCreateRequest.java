package com.kapamejlbka.objectmanager.domain.topology.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class InstallationRouteCreateRequest {

    private String name;
    private String routeType;
    @JsonAlias("surfaceType")
    private String mountSurface;
    private Double lengthMeters;
    private String orientation;
    private String fixingMethod;
    private Long mainMaterialId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getMountSurface() {
        return mountSurface;
    }

    public void setMountSurface(String mountSurface) {
        this.mountSurface = mountSurface;
    }

    public Double getLengthMeters() {
        return lengthMeters;
    }

    public void setLengthMeters(Double lengthMeters) {
        this.lengthMeters = lengthMeters;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getFixingMethod() {
        return fixingMethod;
    }

    public void setFixingMethod(String fixingMethod) {
        this.fixingMethod = fixingMethod;
    }

    public Long getMainMaterialId() {
        return mainMaterialId;
    }

    public void setMainMaterialId(Long mainMaterialId) {
        this.mainMaterialId = mainMaterialId;
    }
}
