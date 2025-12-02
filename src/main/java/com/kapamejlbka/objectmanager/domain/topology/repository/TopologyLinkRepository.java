package com.kapamejlbka.objectmanager.domain.topology.repository;

import com.kapamejlbka.objectmanager.domain.topology.TopologyLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopologyLinkRepository extends JpaRepository<TopologyLink, Long> {

    List<TopologyLink> findByCalculationId(Long calculationId);
}
