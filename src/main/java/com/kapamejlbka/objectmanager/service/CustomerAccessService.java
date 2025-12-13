package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.ProjectCustomer;
import com.kapamejlbka.objectmanager.domain.customer.repository.ProjectCustomerRepository;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.domain.user.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CustomerAccessService {

    private final ProjectCustomerRepository customerRepository;
    private final AppUserRepository userRepository;

    public CustomerAccessService(ProjectCustomerRepository customerRepository, AppUserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectCustomer> findAll() {
        return customerRepository.findAll();
    }

    public List<ProjectCustomer> findVisible(AppUser requester) {
        if (requester == null) {
            return List.of();
        }
        return customerRepository.findAllVisibleForUser(requester);
    }

    @Transactional
    public void grantAccess(UUID customerId, Long userId) {
        ProjectCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        customer.addOwner(user);
        customerRepository.save(customer);
    }

    @Transactional
    public void revokeAccess(UUID customerId, Long userId) {
        ProjectCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        customer.removeOwner(user);
        customerRepository.save(customer);
    }
}
