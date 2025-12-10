package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialNormRepository extends JpaRepository<MaterialNorm, Long> {
    List<MaterialNorm> findAllByContextType(String contextType);
}
