package com.kapamejlbka.objectmanager.domain.material.dto;

import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext;

public class MaterialNormForm {

    private Long id;
    private Long materialId;
    private MaterialNormContext contextType;
    private String formula;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public MaterialNormContext getContextType() {
        return contextType;
    }

    public void setContextType(MaterialNormContext contextType) {
        this.contextType = contextType;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
