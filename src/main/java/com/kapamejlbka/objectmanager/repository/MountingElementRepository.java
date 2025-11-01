package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.MountingElement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MountingElementRepository extends JpaRepository<MountingElement, UUID> {
}
