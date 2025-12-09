package com.kapamejlbka.objectmanager.domain.settings.dto;

public class CalculationSettingsDto {

    private Double standardCabinetDropLengthMeters;
    private Double defaultHorizontalClipStep;
    private Double defaultVerticalClipStep;

    public Double getStandardCabinetDropLengthMeters() {
        return standardCabinetDropLengthMeters;
    }

    public void setStandardCabinetDropLengthMeters(Double standardCabinetDropLengthMeters) {
        this.standardCabinetDropLengthMeters = standardCabinetDropLengthMeters;
    }

    public Double getDefaultHorizontalClipStep() {
        return defaultHorizontalClipStep;
    }

    public void setDefaultHorizontalClipStep(Double defaultHorizontalClipStep) {
        this.defaultHorizontalClipStep = defaultHorizontalClipStep;
    }

    public Double getDefaultVerticalClipStep() {
        return defaultVerticalClipStep;
    }

    public void setDefaultVerticalClipStep(Double defaultVerticalClipStep) {
        this.defaultVerticalClipStep = defaultVerticalClipStep;
    }
}
