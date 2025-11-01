package com.kapamejlbka.objectmanager.web.form;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class MaterialGroupForm {

    private String groupLabel;
    @Valid
    private List<MaterialUsageForm> materials = new ArrayList<>();

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public List<MaterialUsageForm> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialUsageForm> materials) {
        this.materials = materials == null ? new ArrayList<>() : materials;
        if (this.materials.isEmpty()) {
            this.materials.add(new MaterialUsageForm());
        }
    }
}
