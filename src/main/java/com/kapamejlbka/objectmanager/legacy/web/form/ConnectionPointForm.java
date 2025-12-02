package com.kapamejlbka.objectmanager.legacy.web.form;

import java.util.UUID;

public class ConnectionPointForm {

    private String name;
    private UUID mountingElementId;
    private Double distanceToPower;
    private UUID powerCableTypeId;
    private UUID layingMaterialId;
    private String layingSurface;
    private String layingSurfaceCategory;
    private boolean singleSocketEnabled;
    private Integer singleSocketCount;
    private boolean doubleSocketEnabled = true;
    private Integer doubleSocketCount = 1;
    private boolean breakersEnabled = true;
    private Integer breakerCount = 2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getMountingElementId() {
        return mountingElementId;
    }

    public void setMountingElementId(UUID mountingElementId) {
        this.mountingElementId = mountingElementId;
    }

    public Double getDistanceToPower() {
        return distanceToPower;
    }

    public void setDistanceToPower(Double distanceToPower) {
        this.distanceToPower = distanceToPower;
    }

    public UUID getPowerCableTypeId() {
        return powerCableTypeId;
    }

    public void setPowerCableTypeId(UUID powerCableTypeId) {
        this.powerCableTypeId = powerCableTypeId;
    }

    public UUID getLayingMaterialId() {
        return layingMaterialId;
    }

    public void setLayingMaterialId(UUID layingMaterialId) {
        this.layingMaterialId = layingMaterialId;
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

    public boolean isSingleSocketEnabled() {
        return singleSocketEnabled;
    }

    public void setSingleSocketEnabled(boolean singleSocketEnabled) {
        this.singleSocketEnabled = singleSocketEnabled;
    }

    public Integer getSingleSocketCount() {
        return singleSocketCount;
    }

    public void setSingleSocketCount(Integer singleSocketCount) {
        this.singleSocketCount = singleSocketCount;
    }

    public boolean isDoubleSocketEnabled() {
        return doubleSocketEnabled;
    }

    public void setDoubleSocketEnabled(boolean doubleSocketEnabled) {
        this.doubleSocketEnabled = doubleSocketEnabled;
    }

    public Integer getDoubleSocketCount() {
        return doubleSocketCount;
    }

    public void setDoubleSocketCount(Integer doubleSocketCount) {
        this.doubleSocketCount = doubleSocketCount;
    }

    public boolean isBreakersEnabled() {
        return breakersEnabled;
    }

    public void setBreakersEnabled(boolean breakersEnabled) {
        this.breakersEnabled = breakersEnabled;
    }

    public Integer getBreakerCount() {
        return breakerCount;
    }

    public void setBreakerCount(Integer breakerCount) {
        this.breakerCount = breakerCount;
    }

    public void normalizeAccessories() {
        if (singleSocketEnabled) {
            singleSocketCount = ensureMinimum(singleSocketCount, 1);
        } else {
            singleSocketCount = null;
        }
        if (doubleSocketEnabled) {
            doubleSocketCount = ensureMinimum(doubleSocketCount, 1);
        } else {
            doubleSocketCount = null;
        }
        if (breakersEnabled) {
            breakerCount = ensureMinimum(breakerCount, 2);
        } else {
            breakerCount = null;
        }
    }

    public int getEffectiveSingleSocketCount() {
        if (!singleSocketEnabled) {
            return 0;
        }
        return ensureMinimum(singleSocketCount, 1);
    }

    public int getEffectiveDoubleSocketCount() {
        if (!doubleSocketEnabled) {
            return 0;
        }
        return ensureMinimum(doubleSocketCount, 1);
    }

    public int getEffectiveBreakerCount() {
        if (!breakersEnabled) {
            return 0;
        }
        return ensureMinimum(breakerCount, 2);
    }

    public int getBreakerBoxCount() {
        int breakers = getEffectiveBreakerCount();
        if (breakers <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(breakers / 2.0));
    }

    public int getNshviCount() {
        int sockets = getEffectiveSingleSocketCount() + getEffectiveDoubleSocketCount();
        int breakers = getEffectiveBreakerCount();
        return sockets * 4 + breakers * 2;
    }

    private int ensureMinimum(Integer value, int minimum) {
        if (value == null || value < minimum) {
            return minimum;
        }
        return value;
    }
}
