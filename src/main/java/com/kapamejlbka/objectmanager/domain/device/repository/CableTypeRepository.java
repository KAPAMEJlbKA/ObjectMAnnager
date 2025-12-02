package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.CableType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CableTypeRepository extends JpaRepository<CableType, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
