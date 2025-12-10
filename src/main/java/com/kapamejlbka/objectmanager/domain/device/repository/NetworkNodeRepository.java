package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.NetworkNode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {

    List<NetworkNode> findByCalculationId(Long calculationId);

    long countByCalculationId(Long calculationId);

    long countByCalculationSiteId(Long siteId);
}
