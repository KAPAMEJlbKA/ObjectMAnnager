package com.kapamejlbka.objectmannage.service;

import com.kapamejlbka.objectmannage.model.CableFunction;
import com.kapamejlbka.objectmannage.model.CableType;
import com.kapamejlbka.objectmannage.model.DeviceType;
import com.kapamejlbka.objectmannage.repository.CableTypeRepository;
import com.kapamejlbka.objectmannage.repository.DeviceTypeRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ReferenceDataInitializer implements ApplicationRunner {

    private final DeviceTypeRepository deviceTypeRepository;
    private final CableTypeRepository cableTypeRepository;

    public ReferenceDataInitializer(DeviceTypeRepository deviceTypeRepository,
                                    CableTypeRepository cableTypeRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.cableTypeRepository = cableTypeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureDeviceTypes();
        ensureCableTypes();
    }

    private void ensureDeviceTypes() {
        Set<String> existing = deviceTypeRepository.findAll().stream()
                .map(type -> normalize(type.getName()))
                .collect(Collectors.toCollection(HashSet::new));
        ensureDeviceType(existing, "Видеокамера");
        ensureDeviceType(existing, "Сетевое устройство");
        ensureDeviceType(existing, "Точка доступа Wi-Fi");
        ensureDeviceType(existing, "Точка сети");
        ensureDeviceType(existing, "Контроллер считыватель");
        ensureDeviceType(existing, "Турникет");
    }

    private void ensureDeviceType(Set<String> existing, String name) {
        String normalized = normalize(name);
        if (normalized.isEmpty() || existing.contains(normalized)) {
            return;
        }
        deviceTypeRepository.save(new DeviceType(name));
        existing.add(normalized);
    }

    private void ensureCableTypes() {
        Set<String> existing = cableTypeRepository.findAll().stream()
                .map(type -> normalize(type.getName()))
                .collect(Collectors.toCollection(HashSet::new));
        ensureCableType(existing, "Кабель UTP", CableFunction.SIGNAL);
        ensureCableType(existing, "Кабель ШВВП", CableFunction.LOW_VOLTAGE_POWER);
    }

    private void ensureCableType(Set<String> existing, String name, CableFunction function) {
        String normalized = normalize(name);
        if (normalized.isEmpty() || existing.contains(normalized)) {
            return;
        }
        CableType cableType = new CableType(name, function);
        cableTypeRepository.save(cableType);
        existing.add(normalized);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
