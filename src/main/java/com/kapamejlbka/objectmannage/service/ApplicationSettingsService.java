package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ApplicationSetting;
import com.kapamejlbka.objectmannage.model.MapProvider;
import com.kapamejlbka.objectmannage.repository.ApplicationSettingRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApplicationSettingsService {

    private static final String MAP_PROVIDER_KEY = "map.provider";
    private static final String MATERIAL_CLIPS_PER_METER_KEY = "materials.coefficient.corrugated.clips";
    private static final String MATERIAL_TIES_PER_METER_KEY = "materials.coefficient.cable.ties";
    private static final String BRANDING_LOGO_DATA_KEY = "branding.logo.data";
    private static final String BRANDING_LOGO_CONTENT_TYPE_KEY = "branding.logo.contentType";

    private final ApplicationSettingRepository settingRepository;

    public ApplicationSettingsService(ApplicationSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public MapProvider getMapProvider() {
        return getSettingValue(MAP_PROVIDER_KEY)
                .map(value -> {
                    try {
                        return MapProvider.valueOf(value);
                    } catch (IllegalArgumentException ex) {
                        return MapProvider.YANDEX;
                    }
                })
                .orElse(MapProvider.YANDEX);
    }

    @Transactional
    public void updateMapProvider(MapProvider provider) {
        saveSettingValue(MAP_PROVIDER_KEY, provider.name());
    }

    public MaterialCoefficients getMaterialCoefficients() {
        double clipsPerMeter = parseDoubleSetting(getSettingValue(MATERIAL_CLIPS_PER_METER_KEY).orElse(null));
        double tiesPerMeter = parseDoubleSetting(getSettingValue(MATERIAL_TIES_PER_METER_KEY).orElse(null));
        return new MaterialCoefficients(clipsPerMeter, tiesPerMeter);
    }

    @Transactional
    public void updateMaterialCoefficients(double clipsPerMeter, double tiesPerMeter) {
        saveSettingValue(MATERIAL_CLIPS_PER_METER_KEY, formatDouble(clipsPerMeter));
        saveSettingValue(MATERIAL_TIES_PER_METER_KEY, formatDouble(tiesPerMeter));
    }

    public Optional<CompanyLogo> getCompanyLogo() {
        Optional<String> encoded = getSettingValue(BRANDING_LOGO_DATA_KEY);
        if (encoded.isEmpty()) {
            return Optional.empty();
        }
        try {
            byte[] data = Base64.getDecoder().decode(encoded.get());
            String contentType = getSettingValue(BRANDING_LOGO_CONTENT_TYPE_KEY)
                    .filter(StringUtils::hasText)
                    .orElse("image/png");
            return Optional.of(new CompanyLogo(data, contentType));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Transactional
    public void storeCompanyLogo(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            removeCompanyLogo();
            return;
        }
        String encoded = Base64.getEncoder().encodeToString(data);
        saveSettingValue(BRANDING_LOGO_DATA_KEY, encoded);
        String normalizedContentType = StringUtils.hasText(contentType) ? contentType.trim() : "application/octet-stream";
        saveSettingValue(BRANDING_LOGO_CONTENT_TYPE_KEY, normalizedContentType);
    }

    @Transactional
    public void removeCompanyLogo() {
        deleteSetting(BRANDING_LOGO_DATA_KEY);
        deleteSetting(BRANDING_LOGO_CONTENT_TYPE_KEY);
    }

    private Optional<String> getSettingValue(String key) {
        return settingRepository.findBySettingKey(key)
                .map(ApplicationSetting::getSettingValue)
                .filter(StringUtils::hasText)
                .map(value -> new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }

    private void saveSettingValue(String key, String value) {
        ApplicationSetting setting = settingRepository.findBySettingKey(key)
                .orElse(new ApplicationSetting());
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        settingRepository.save(setting);
    }

    private void deleteSetting(String key) {
        settingRepository.findBySettingKey(key).ifPresent(settingRepository::delete);
    }

    private double parseDoubleSetting(String value) {
        if (!StringUtils.hasText(value)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String formatDouble(double value) {
        return String.format(java.util.Locale.US, "%.4f", value);
    }

    public record MaterialCoefficients(double clipsPerMeter, double tiesPerMeter) {
    }

    public record CompanyLogo(byte[] data, String contentType) {
    }
}
