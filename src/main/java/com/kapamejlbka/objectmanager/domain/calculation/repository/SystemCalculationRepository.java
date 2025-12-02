package com.kapamejlbka.objectmanager.domain.calculation.repository;

import com.kapamejlbka.objectmanager.domain.calculation.SystemCalculation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemCalculationRepository extends JpaRepository<SystemCalculation, Long> {

    List<SystemCalculation> findBySiteId(Long siteId);
}
