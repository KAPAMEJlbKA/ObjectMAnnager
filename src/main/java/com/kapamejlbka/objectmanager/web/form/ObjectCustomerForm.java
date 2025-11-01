package com.kapamejlbka.objectmanager.web.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public class ObjectCustomerForm {

    private String name;
    private String enterpriseName;
    private String taxNumber;
    private String contactEmail;
    private List<String> contactPhones = new ArrayList<>();

    public ObjectCustomerForm() {
        contactPhones.add("");
    }

    public List<String> sanitizedPhones() {
        if (contactPhones == null) {
            return Collections.emptyList();
        }
        return contactPhones.stream()
                .map(phone -> phone == null ? null : phone.trim())
                .filter(phone -> phone != null && !phone.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isEmailValid() {
        String email = sanitizedEmail();
        return email == null || email.matches("^[^@]+@[^@]+$");
    }

    public String sanitizedName() {
        return StringUtils.hasText(name) ? name.trim() : null;
    }

    public String sanitizedEnterpriseName() {
        return StringUtils.hasText(enterpriseName) ? enterpriseName.trim() : null;
    }

    public String sanitizedTaxNumber() {
        return StringUtils.hasText(taxNumber) ? taxNumber.trim() : null;
    }

    public String sanitizedEmail() {
        return StringUtils.hasText(contactEmail) ? contactEmail.trim() : null;
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
        return contactPhones;
    }

    public void setContactPhones(List<String> contactPhones) {
        this.contactPhones = contactPhones == null ? new ArrayList<>() : contactPhones;
        if (this.contactPhones.isEmpty()) {
            this.contactPhones.add("");
        }
    }
}
