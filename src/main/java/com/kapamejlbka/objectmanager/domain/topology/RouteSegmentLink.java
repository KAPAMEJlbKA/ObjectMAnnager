package com.kapamejlbka.objectmanager.domain.topology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_segment_links")
public class RouteSegmentLink {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private InstallationRoute route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topology_link_id", nullable = false)
    private TopologyLink topologyLink;

    @Column(name = "portion_ratio")
    private Double portionRatio;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public InstallationRoute getRoute() {
        return route;
    }

    public void setRoute(InstallationRoute route) {
        this.route = route;
    }

    public TopologyLink getTopologyLink() {
        return topologyLink;
    }

    public void setTopologyLink(TopologyLink topologyLink) {
        this.topologyLink = topologyLink;
    }

    public Double getPortionRatio() {
        return portionRatio;
    }

    public void setPortionRatio(Double portionRatio) {
        this.portionRatio = portionRatio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
