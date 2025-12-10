package com.kapamejlbka.objectmanager.config;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultMaterialsInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMaterialsInitializer.class);

    private static final String DEFAULT_CATEGORY = "BASIC";
    private static final String CATEGORY_CABLE_UTP = "CABLE_UTP";
    private static final String CATEGORY_CABLE_POWER = "CABLE_POWER";
    private static final String CATEGORY_CABLE_FIBER = "CABLE_FIBER";

    private final MaterialRepository materialRepository;
    private final MaterialNormRepository materialNormRepository;

    public DefaultMaterialsInitializer(
            MaterialRepository materialRepository, MaterialNormRepository materialNormRepository) {
        this.materialRepository = materialRepository;
        this.materialNormRepository = materialNormRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (materialRepository.count() > 0) {
            LOG.info("Materials table is not empty, skipping default initialization");
            return;
        }

        LOG.info("Initializing default materials and norms");
        Map<String, Material> materials = createDefaultMaterials();
        createDefaultNorms(materials);
    }

    private Map<String, Material> createDefaultMaterials() {
        Map<String, Material> materials = new HashMap<>();
        materials.put(
                "MAT_CAMERA_ANCHOR",
                getOrCreateMaterial(
                        "MAT_CAMERA_ANCHOR",
                        "Крепёж для видеокамеры (дюбель/саморез)",
                        "шт",
                        DEFAULT_CATEGORY));
        materials.put(
                "MAT_RJ45_PLUG",
                getOrCreateMaterial("MAT_RJ45_PLUG", "Коннектор RJ-45", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_READER_ANCHOR",
                getOrCreateMaterial("MAT_READER_ANCHOR", "Крепёж для считывателя", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_CABINET_ANCHOR",
                getOrCreateMaterial("MAT_CABINET_ANCHOR", "Крепёж шкафа (анкера)", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_INPUT_GLAND",
                getOrCreateMaterial("MAT_INPUT_GLAND", "Вводная муфта в шкаф", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_POWER_LUG",
                getOrCreateMaterial("MAT_POWER_LUG", "Наконечник силовой", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_SOCKET_DOUBLE",
                getOrCreateMaterial("MAT_SOCKET_DOUBLE", "Розетка 220В двойная", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_BREAKER",
                getOrCreateMaterial("MAT_BREAKER", "Автоматический выключатель", "шт", DEFAULT_CATEGORY));
        materials.put(
                "MAT_CABINET_400",
                getOrCreateMaterial("MAT_CABINET_400", "Шкаф навесной 400мм", "шт", DEFAULT_CATEGORY));
        materials.put(
                "CABLE_UTP_CAT5E",
                getOrCreateMaterial("CABLE_UTP_CAT5E", "Кабель UTP Cat5e", "м", CATEGORY_CABLE_UTP));
        materials.put(
                "CABLE_UTP_CAT6",
                getOrCreateMaterial("CABLE_UTP_CAT6", "Кабель UTP Cat6", "м", CATEGORY_CABLE_UTP));
        materials.put(
                "CABLE_POWER_3X1_5",
                getOrCreateMaterial("CABLE_POWER_3X1_5", "Кабель питания 3×1.5", "м", CATEGORY_CABLE_POWER));
        materials.put(
                "CABLE_POWER_3X2_5",
                getOrCreateMaterial("CABLE_POWER_3X2_5", "Кабель питания 3×2.5", "м", CATEGORY_CABLE_POWER));
        materials.put(
                "CABLE_FIBER_4",
                getOrCreateMaterial("CABLE_FIBER_4", "Оптический кабель 4 волокна", "м", CATEGORY_CABLE_FIBER));
        materials.put(
                "CABLE_FIBER_8",
                getOrCreateMaterial("CABLE_FIBER_8", "Оптический кабель 8 волокон", "м", CATEGORY_CABLE_FIBER));
        return materials;
    }

    private Material getOrCreateMaterial(String code, String name, String unit, String category) {
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        return materialRepository
                .findByCode(normalizedCode)
                .orElseGet(
                        () -> {
                            Material material = new Material();
                            material.setCode(normalizedCode);
                            material.setName(name);
                            material.setUnit(unit);
                            material.setCategory(category);
                            return materialRepository.save(material);
                        });
    }

    private void createDefaultNorms(Map<String, Material> materials) {
        createNormIfMissing("ENDPOINT_CAMERA_FIXING", materials.get("MAT_CAMERA_ANCHOR"), "4",
                "4 точки крепления на камеру");
        createNormIfMissing("ENDPOINT_CAMERA_RJ45", materials.get("MAT_RJ45_PLUG"), "2",
                "2 коннектора RJ-45 на камеру");
        createNormIfMissing("ENDPOINT_READER_FIXING", materials.get("MAT_READER_ANCHOR"), "4",
                "4 точки крепления на считыватель");
        createNormIfMissing("ENDPOINT_READER_RJ45", materials.get("MAT_RJ45_PLUG"), "2",
                "2 коннектора RJ-45 на считыватель");
        createNormIfMissing("NODE_CABINET_FIXING_WALL", materials.get("MAT_CABINET_ANCHOR"), "4",
                "Шкаф на 4 анкера при монтаже на стену");
        createNormIfMissing("NODE_INPUT_GLAND", materials.get("MAT_INPUT_GLAND"), "incomingLinesCount",
                "Количество муфт соответствует количеству приходящих линий");
        createNormIfMissing("NODE_LUGS", materials.get("MAT_POWER_LUG"), "lugCount",
                "Набор наконечников силовых, временная формула");
        createNormIfMissing("NODE_SOCKET_DOUBLE", materials.get("MAT_SOCKET_DOUBLE"), "baseSockets + extraSockets",
                "Количество двойных розеток: базовые + доп.");
        createNormIfMissing("NODE_CIRCUIT_BREAKER", materials.get("MAT_BREAKER"), "baseBreakers + extraBreakers",
                "Количество автоматов: базовые + доп.");
        createNormIfMissing("NODE_CABINET_400", materials.get("MAT_CABINET_400"), "1",
                "Один шкаф 400 мм на узел");
        createNormIfMissing("LINK_UTP_LENGTH", materials.get("CABLE_UTP_CAT5E"), "length",
                "Длина UTP-линии в метрах");
        createNormIfMissing("LINK_POWER_LENGTH", materials.get("CABLE_POWER_3X1_5"), "length",
                "Длина силовой линии в метрах");
        createNormIfMissing("FIBER_4", materials.get("CABLE_FIBER_4"), "length",
                "Оптический кабель 4 волокна по длине");
        createNormIfMissing("FIBER_8", materials.get("CABLE_FIBER_8"), "length",
                "Оптический кабель 8 волокон по длине");
    }

    private void createNormIfMissing(String contextType, Material material, String formula, String description) {
        if (material == null) {
            LOG.warn("Material is missing for default norm {}", contextType);
            return;
        }
        boolean normMissing = materialNormRepository.findAllByContextType(contextType).isEmpty();
        if (normMissing) {
            MaterialNorm norm = new MaterialNorm();
            norm.setContextType(contextType);
            norm.setMaterial(material);
            norm.setFormula(formula);
            norm.setDescription(description);
            materialNormRepository.save(norm);
        }
    }
}
