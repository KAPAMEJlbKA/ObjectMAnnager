package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.ApplicationSetting;
import com.kapamejlbka.objectmannage.model.MapProvider;
import com.kapamejlbka.objectmannage.repository.ApplicationSettingRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationSettingsService {

    private static final String MAP_PROVIDER_KEY = "map.provider";

    private final ApplicationSettingRepository settingRepository;

    public ApplicationSettingsService(ApplicationSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public MapProvider getMapProvider() {
        Optional<ApplicationSetting> setting = settingRepository.findBySettingKey(MAP_PROVIDER_KEY);
        return setting.map(ApplicationSetting::getSettingValue)
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
        ApplicationSetting setting = settingRepository.findBySettingKey(MAP_PROVIDER_KEY)
                .orElse(new ApplicationSetting());
        setting.setSettingKey(MAP_PROVIDER_KEY);
        setting.setSettingValue(provider.name());
        settingRepository.save(setting);
    }
}
