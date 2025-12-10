package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.EndpointDevice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndpointDeviceRepository extends JpaRepository<EndpointDevice, Long> {

    List<EndpointDevice> findByCalculationId(Long calculationId);

    long countByCalculationId(Long calculationId);

    long countByCalculationSiteId(Long siteId);
}
