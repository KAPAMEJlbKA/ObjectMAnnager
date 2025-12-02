package com.kapamejlbka.objectmanager.domain.customer;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project_customers")
public class ProjectCustomer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "enterprise_name")
    private String enterpriseName;

    @Column(name = "tax_number")
    private String taxNumber;

    private String contactEmail;

    @ElementCollection
    @CollectionTable(name = "project_customer_phones", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "phone_number", length = 64)
    private List<String> contactPhones = new ArrayList<>();

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer")
    private List<ManagedObject> objects = new ArrayList<>();

    public ProjectCustomer() {
    }

    public ProjectCustomer(String name, String enterpriseName, String taxNumber,
                           String contactEmail, List<String> contactPhones) {
        this.name = name;
        this.enterpriseName = enterpriseName;
        this.taxNumber = taxNumber;
        this.contactEmail = contactEmail;
        setContactPhones(contactPhones);
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public List<String> getContactPhones() {
        return Collections.unmodifiableList(contactPhones);
    }

    public void setContactPhones(List<String> phones) {
        this.contactPhones.clear();
        if (phones != null) {
            phones.stream()
                    .map(phone -> phone == null ? null : phone.trim())
                    .filter(phone -> phone != null && !phone.isEmpty())
                    .forEach(this.contactPhones::add);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ManagedObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }
}
