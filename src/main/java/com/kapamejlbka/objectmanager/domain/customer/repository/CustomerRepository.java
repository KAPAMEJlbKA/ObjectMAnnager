package com.kapamejlbka.objectmanager.domain.customer.repository;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByNameContainingIgnoreCaseOrTaxNumberContainingIgnoreCase(String name, String taxNumber);
}
