package com.kapamejlbka.objectmanager.repository;

import com.kapamejlbka.objectmanager.model.ProjectCustomer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCustomerRepository extends JpaRepository<ProjectCustomer, UUID> {

    Optional<ProjectCustomer> findByNameIgnoreCase(String name);
}
