package com.kapamejlbka.objectmanager.web.form;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MountingSelectionForm {

    private UUID elementId;
    private String elementName;
    private String quantity;
    @Valid
    private List<MountingMaterialForm> materials = new ArrayList<>();
    private boolean autoAssigned;

    public MountingSelectionForm() {
        ensureMaterialRows();
    }

    public UUID getElementId() {
        return elementId;
    }

    public void setElementId(UUID elementId) {
        this.elementId = elementId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public List<MountingMaterialForm> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MountingMaterialForm> materials) {
        this.materials = materials == null ? new ArrayList<>() : materials;
        ensureMaterialRows();
    }

    public boolean isAutoAssigned() {
        return autoAssigned;
    }

    public void setAutoAssigned(boolean autoAssigned) {
        this.autoAssigned = autoAssigned;
    }

    public void ensureMaterialRows() {
        if (materials == null) {
            materials = new ArrayList<>();
        }
        if (materials.isEmpty()) {
            materials.add(new MountingMaterialForm());
        }
    }

    public boolean hasMaterials() {
        if (materials == null) {
            return false;
        }
        return materials.stream().anyMatch(material -> material != null && !material.isEmpty());
    }
}
