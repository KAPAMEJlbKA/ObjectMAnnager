package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.device.CableFunction;
import com.kapamejlbka.objectmanager.domain.device.CableType;
import com.kapamejlbka.objectmanager.domain.device.DeviceType;
import com.kapamejlbka.objectmanager.domain.device.repository.CableTypeRepository;
import com.kapamejlbka.objectmanager.domain.device.repository.DeviceTypeRepository;
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
    private final UserService userService;

    public ReferenceDataInitializer(DeviceTypeRepository deviceTypeRepository,
                                    CableTypeRepository cableTypeRepository,
                                    UserService userService) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.cableTypeRepository = cableTypeRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userService.createAdminIfNotExists();
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
