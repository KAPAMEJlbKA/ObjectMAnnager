package com.kapamejlbka.objectmanager.legacy.web.form;

import java.util.UUID;
import org.springframework.util.StringUtils;

public class MaterialUsageForm {

    private UUID materialId;
    private String amount;
    private String layingSurface;
    private String layingSurfaceCategory;

    public boolean isEmpty() {
        return materialId == null
                && !StringUtils.hasText(amount)
                && !StringUtils.hasText(layingSurface)
                && !StringUtils.hasText(layingSurfaceCategory);
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getLayingSurface() {
        return layingSurface;
    }

    public void setLayingSurface(String layingSurface) {
        this.layingSurface = layingSurface;
    }

    public String getLayingSurfaceCategory() {
        return layingSurfaceCategory;
    }

    public void setLayingSurfaceCategory(String layingSurfaceCategory) {
        this.layingSurfaceCategory = layingSurfaceCategory;
    }
}
