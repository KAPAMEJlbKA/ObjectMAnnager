package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.MountingElement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MountingElementRepository extends JpaRepository<MountingElement, UUID> {
}
