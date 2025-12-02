package com.kapamejlbka.objectmanager.domain.topology.repository;

import com.kapamejlbka.objectmanager.domain.topology.InstallationRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationRouteRepository extends JpaRepository<InstallationRoute, Long> {

    List<InstallationRoute> findByCalculationId(Long calculationId);
}
