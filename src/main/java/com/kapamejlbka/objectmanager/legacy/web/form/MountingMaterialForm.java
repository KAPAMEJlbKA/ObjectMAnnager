package com.kapamejlbka.objectmanager.legacy.web.form;

import java.util.UUID;
import org.springframework.util.StringUtils;

public class MountingMaterialForm {

    private UUID materialId;
    private String materialName;
    private String amount;

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isEmpty() {
        return materialId == null && !StringUtils.hasText(amount);
    }
}
