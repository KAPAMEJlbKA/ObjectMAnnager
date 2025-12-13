package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.ProjectCustomer;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectCustomerRepository extends JpaRepository<ProjectCustomer, UUID> {

    Optional<ProjectCustomer> findByNameIgnoreCase(String name);

    @Query("""
            select distinct customer from ProjectCustomer customer
            left join customer.owners owners
            where customer.createdBy = :user or owners = :user
            order by customer.createdAt desc
            """)
    List<ProjectCustomer> findAllVisibleForUser(@Param("user") AppUser user);
}
