package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.CableType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CableTypeRepository extends JpaRepository<CableType, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
