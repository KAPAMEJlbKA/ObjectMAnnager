package com.kapamejlbka.objectmanager.domain.user.repository;

import com.kapamejlbka.objectmanager.domain.user.AppRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByNameIgnoreCase(String name);
}
