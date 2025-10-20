package com.kapamejlbka.objectmannage.repository;

import com.kapamejlbka.objectmannage.model.ProjectCustomer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCustomerRepository extends JpaRepository<ProjectCustomer, UUID> {

    Optional<ProjectCustomer> findByNameIgnoreCase(String name);
}
