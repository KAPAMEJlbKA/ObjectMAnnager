package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialNormRepository extends JpaRepository<MaterialNorm, Long> {
    Optional<MaterialNorm> findByContextType(String contextType);
}
