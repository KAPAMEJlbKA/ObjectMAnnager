package com.kapamejlbka.objectmanager.domain.user.repository;

import com.kapamejlbka.objectmanager.domain.user.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);
}
