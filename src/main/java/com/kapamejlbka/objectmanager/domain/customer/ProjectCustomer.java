package com.kapamejlbka.objectmanager.domain.customer;

import com.kapamejlbka.objectmanager.domain.user.AppUser;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @ManyToMany
    @JoinTable(
            name = "customer_user_access",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> owners = new HashSet<>();

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

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
        if (createdBy != null) {
            addOwner(createdBy);
        }
    }

    public Set<AppUser> getOwners() {
        return Collections.unmodifiableSet(owners);
    }

    public void addOwner(AppUser owner) {
        if (owner != null) {
            owners.add(owner);
        }
    }

    public void removeOwner(AppUser owner) {
        if (owner != null) {
            owners.remove(owner);
        }
    }

    public List<ManagedObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }
}
