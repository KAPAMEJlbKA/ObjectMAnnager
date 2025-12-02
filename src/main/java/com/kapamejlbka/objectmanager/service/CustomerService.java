package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.customer.Customer;
import com.kapamejlbka.objectmanager.domain.customer.dto.CustomerCreateRequest;
import com.kapamejlbka.objectmanager.domain.customer.dto.CustomerUpdateRequest;
import com.kapamejlbka.objectmanager.domain.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer create(CustomerCreateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Customer data is required");
        }
        Customer customer = new Customer();
        applyDto(customer, dto);
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerUpdateRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Customer data is required");
        }
        Customer customer = getById(id);
        applyDto(customer, dto);
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    public List<Customer> search(String query) {
        String normalizedQuery = normalize(query);
        if (!StringUtils.hasText(normalizedQuery)) {
            return customerRepository.findAll();
        }
        return customerRepository.findByNameContainingIgnoreCaseOrTaxNumberContainingIgnoreCase(
                normalizedQuery, normalizedQuery);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = getById(id);
        customerRepository.delete(customer);
    }

    private void applyDto(Customer customer, CustomerCreateRequest dto) {
        String name = normalize(dto.getName());
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Customer name is required");
        }
        customer.setName(name);
        customer.setTaxNumber(normalize(dto.getTaxNumber()));
        customer.setCountry(normalize(dto.getCountry()));
        customer.setCity(normalize(dto.getCity()));
        customer.setAddressLine(normalize(dto.getAddressLine()));
        customer.setContactName(normalize(dto.getContactName()));
        customer.setContactPosition(normalize(dto.getContactPosition()));
        customer.setContactPhone(normalize(dto.getContactPhone()));
        customer.setContactEmail(normalize(dto.getContactEmail()));
        customer.setNotes(normalize(dto.getNotes()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
