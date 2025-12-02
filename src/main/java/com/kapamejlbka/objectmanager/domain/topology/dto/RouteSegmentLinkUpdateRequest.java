package com.kapamejlbka.objectmanager.domain.topology.dto;

public class RouteSegmentLinkUpdateRequest {

    private Long routeId;
    private Long topologyLinkId;
    private Double portionRatio;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Long getTopologyLinkId() {
        return topologyLinkId;
    }

    public void setTopologyLinkId(Long topologyLinkId) {
        this.topologyLinkId = topologyLinkId;
    }

    public Double getPortionRatio() {
        return portionRatio;
    }

    public void setPortionRatio(Double portionRatio) {
        this.portionRatio = portionRatio;
    }
}
