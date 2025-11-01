package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.CableType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CableTypeRepository extends JpaRepository<CableType, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
