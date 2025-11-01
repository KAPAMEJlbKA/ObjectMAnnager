package com.kapamejlbka.objectmanager.web.form;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class ObjectForm {

    @NotBlank
    private String name;
    private String description;
    private UUID customerId;
    private boolean createNewCustomer;
    private ObjectCustomerForm newCustomer = new ObjectCustomerForm();
    private Double latitude;
    private Double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public boolean isCreateNewCustomer() {
        return createNewCustomer;
    }

    public void setCreateNewCustomer(boolean createNewCustomer) {
        this.createNewCustomer = createNewCustomer;
    }

    public ObjectCustomerForm getNewCustomer() {
        if (newCustomer == null) {
            newCustomer = new ObjectCustomerForm();
        }
        return newCustomer;
    }

    public void setNewCustomer(ObjectCustomerForm newCustomer) {
        this.newCustomer = newCustomer;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
