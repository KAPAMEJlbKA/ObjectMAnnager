package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.domain.material.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByCode(String code);
}
