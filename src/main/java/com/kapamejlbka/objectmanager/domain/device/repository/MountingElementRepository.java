package com.kapamejlbka.objectmanager.domain.device.repository;

import com.kapamejlbka.objectmanager.domain.device.MountingElement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MountingElementRepository extends JpaRepository<MountingElement, UUID> {
}
