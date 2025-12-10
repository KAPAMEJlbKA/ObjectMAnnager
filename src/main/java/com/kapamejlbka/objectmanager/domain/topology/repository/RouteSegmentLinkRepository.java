package com.kapamejlbka.objectmanager.domain.topology.repository;

import com.kapamejlbka.objectmanager.domain.topology.RouteSegmentLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteSegmentLinkRepository extends JpaRepository<RouteSegmentLink, Long> {

    List<RouteSegmentLink> findByRouteId(Long routeId);

    List<RouteSegmentLink> findByRouteCalculation_Id(Long calculationId);

    List<RouteSegmentLink> findByTopologyLinkId(Long topologyLinkId);

    void deleteByRouteId(Long routeId);
}
