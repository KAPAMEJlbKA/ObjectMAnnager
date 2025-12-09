package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.settings.CalculationSettings;
import com.kapamejlbka.objectmanager.domain.settings.dto.CalculationSettingsDto;
import com.kapamejlbka.objectmanager.repository.CalculationSettingsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private static final long SINGLE_ROW_ID = 1L;

    private final CalculationSettingsRepository repository;

    public SettingsService(CalculationSettingsRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void ensureSettings() {
        getOrCreate();
    }

    @Transactional(readOnly = true)
    public CalculationSettingsDto getSettings() {
        CalculationSettings settings = getOrCreate();
        CalculationSettingsDto dto = new CalculationSettingsDto();
        dto.setStandardCabinetDropLengthMeters(settings.getStandardCabinetDropLengthMeters());
        dto.setDefaultHorizontalClipStep(settings.getDefaultHorizontalClipStep());
        dto.setDefaultVerticalClipStep(settings.getDefaultVerticalClipStep());
        return dto;
    }

    @Transactional
    public CalculationSettingsDto updateSettings(CalculationSettingsDto dto) {
        CalculationSettings settings = getOrCreate();
        settings.setStandardCabinetDropLengthMeters(normalizeNullablePositive(dto.getStandardCabinetDropLengthMeters()));
        settings.setDefaultHorizontalClipStep(normalizeNullablePositive(dto.getDefaultHorizontalClipStep()));
        settings.setDefaultVerticalClipStep(normalizeNullablePositive(dto.getDefaultVerticalClipStep()));
        repository.save(settings);
        return getSettings();
    }

    private CalculationSettings getOrCreate() {
        return repository.findById(SINGLE_ROW_ID).orElseGet(() -> {
            CalculationSettings settings = new CalculationSettings();
            settings.setId(SINGLE_ROW_ID);
            settings.setStandardCabinetDropLengthMeters(0.0);
            settings.setDefaultHorizontalClipStep(null);
            settings.setDefaultVerticalClipStep(null);
            return repository.save(settings);
        });
    }

    private Double normalizeNullablePositive(Double value) {
        if (value == null) {
            return null;
        }
        return value < 0 ? 0.0 : value;
    }
}
