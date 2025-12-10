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

    private static final String CATEGORY_VIDEO = "Видеонаблюдение";
    private static final String CATEGORY_ACCESS = "СКУД";
    private static final String CATEGORY_NETWORK = "Сеть и СКС";
    private static final String CATEGORY_POWER = "Питание";
    private static final String CATEGORY_OPTICS = "Оптика";
    private static final String CATEGORY_ROUTE_CORRUGATED = "Трассы — гофра и трубы";
    private static final String CATEGORY_ROUTE_CHANNEL = "Трассы — кабель-канал";
    private static final String CATEGORY_ROUTE_TRAY = "Трассы — лотки/конструкции";
    private static final String CATEGORY_ROUTE_WIRE_ROPE = "Трассы — трос";
    private static final String CATEGORY_ROUTE_OPEN = "Трассы — открытая прокладка";
    private static final String CATEGORY_CABINET = "Узлы и шкафы";

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
        LOG.info("Ensuring default materials and norms are present");
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
                        CATEGORY_VIDEO));
        materials.put(
                "MAT_ACCESS_POINT_ANCHOR",
                getOrCreateMaterial(
                        "MAT_ACCESS_POINT_ANCHOR",
                        "Крепёж для точки доступа",
                        "шт",
                        CATEGORY_NETWORK));
        materials.put(
                "MAT_RJ45_PLUG",
                getOrCreateMaterial("MAT_RJ45_PLUG", "Коннектор RJ-45", "шт", CATEGORY_NETWORK));
        materials.put(
                "MAT_READER_ANCHOR",
                getOrCreateMaterial("MAT_READER_ANCHOR", "Крепёж для считывателя", "шт", CATEGORY_ACCESS));
        materials.put(
                "MAT_TURNSTILE_ANCHOR",
                getOrCreateMaterial("MAT_TURNSTILE_ANCHOR", "Крепёж для турникета", "шт", CATEGORY_ACCESS));
        materials.put(
                "MAT_CABINET_ANCHOR",
                getOrCreateMaterial("MAT_CABINET_ANCHOR", "Крепёж шкафа (анкера)", "шт", CATEGORY_CABINET));
        materials.put(
                "MAT_INPUT_GLAND",
                getOrCreateMaterial("MAT_INPUT_GLAND", "Вводная муфта в шкаф", "шт", CATEGORY_CABINET));
        materials.put(
                "MAT_POWER_LUG",
                getOrCreateMaterial("MAT_POWER_LUG", "Наконечник силовой", "шт", CATEGORY_POWER));
        materials.put(
                "MAT_SOCKET_DOUBLE",
                getOrCreateMaterial("MAT_SOCKET_DOUBLE", "Розетка 220В двойная", "шт", CATEGORY_POWER));
        materials.put(
                "MAT_BREAKER",
                getOrCreateMaterial("MAT_BREAKER", "Автоматический выключатель", "шт", CATEGORY_POWER));
        materials.put(
                "MAT_CABINET_400",
                getOrCreateMaterial("MAT_CABINET_400", "Шкаф навесной 400мм", "шт", CATEGORY_CABINET));
        materials.put(
                "CABLE_UTP_CAT5E",
                getOrCreateMaterial("CABLE_UTP_CAT5E", "Кабель UTP Cat5e", "м", CATEGORY_NETWORK));
        materials.put(
                "CABLE_UTP_CAT6",
                getOrCreateMaterial("CABLE_UTP_CAT6", "Кабель UTP Cat6", "м", CATEGORY_NETWORK));
        materials.put(
                "CABLE_POWER_3X1_5",
                getOrCreateMaterial("CABLE_POWER_3X1_5", "Кабель питания 3×1.5", "м", CATEGORY_POWER));
        materials.put(
                "CABLE_POWER_3X2_5",
                getOrCreateMaterial("CABLE_POWER_3X2_5", "Кабель питания 3×2.5", "м", CATEGORY_POWER));
        materials.put(
                "CABLE_FIBER_4",
                getOrCreateMaterial("CABLE_FIBER_4", "Оптический кабель 4 волокна", "м", CATEGORY_OPTICS));
        materials.put(
                "CABLE_FIBER_8",
                getOrCreateMaterial("CABLE_FIBER_8", "Оптический кабель 8 волокон", "м", CATEGORY_OPTICS));
        materials.put(
                "MAT_CORRUGATED_PIPE_20",
                getOrCreateMaterial("MAT_CORRUGATED_PIPE_20", "Гофротруба 20 мм", "м", CATEGORY_ROUTE_CORRUGATED));
        materials.put(
                "MAT_CORRUGATED_CLIP",
                getOrCreateMaterial("MAT_CORRUGATED_CLIP", "Клипса для гофры", "шт", CATEGORY_ROUTE_CORRUGATED));
        materials.put(
                "MAT_CORRUGATED_COUPLING",
                getOrCreateMaterial("MAT_CORRUGATED_COUPLING", "Муфта для гофры", "шт", CATEGORY_ROUTE_CORRUGATED));
        materials.put(
                "MAT_CORRUGATED_BRANCH",
                getOrCreateMaterial("MAT_CORRUGATED_BRANCH", "Ответвление для гофры", "шт", CATEGORY_ROUTE_CORRUGATED));
        materials.put(
                "MAT_CABLE_CHANNEL_40X25",
                getOrCreateMaterial("MAT_CABLE_CHANNEL_40X25", "Кабель-канал 40×25", "м", CATEGORY_ROUTE_CHANNEL));
        materials.put(
                "MAT_CABLE_CHANNEL_CLIP",
                getOrCreateMaterial("MAT_CABLE_CHANNEL_CLIP", "Клипса для кабель-канала", "шт", CATEGORY_ROUTE_CHANNEL));
        materials.put(
                "MAT_CABLE_CHANNEL_FASTENER",
                getOrCreateMaterial("MAT_CABLE_CHANNEL_FASTENER", "Крепёж кабель-канала", "шт", CATEGORY_ROUTE_CHANNEL));
        materials.put(
                "MAT_CABLE_TRAY_100",
                getOrCreateMaterial("MAT_CABLE_TRAY_100", "Кабельный лоток 100 мм", "м", CATEGORY_ROUTE_TRAY));
        materials.put(
                "MAT_TRAY_TIE",
                getOrCreateMaterial("MAT_TRAY_TIE", "Нейлоновая стяжка для лотков", "шт", CATEGORY_ROUTE_TRAY));
        materials.put(
                "MAT_WIRE_ROPE_4MM",
                getOrCreateMaterial("MAT_WIRE_ROPE_4MM", "Несущий трос 4 мм", "м", CATEGORY_ROUTE_WIRE_ROPE));
        materials.put(
                "MAT_WIRE_ROPE_ANCHOR",
                getOrCreateMaterial("MAT_WIRE_ROPE_ANCHOR", "Анкер для троса", "шт", CATEGORY_ROUTE_WIRE_ROPE));
        materials.put(
                "MAT_WIRE_ROPE_TURNBUCKLE",
                getOrCreateMaterial("MAT_WIRE_ROPE_TURNBUCKLE", "Талреп для троса", "шт", CATEGORY_ROUTE_WIRE_ROPE));
        materials.put(
                "MAT_WIRE_ROPE_CLAMP",
                getOrCreateMaterial("MAT_WIRE_ROPE_CLAMP", "Зажим для троса", "шт", CATEGORY_ROUTE_WIRE_ROPE));
        materials.put(
                "MAT_BARE_CABLE_CLIP",
                getOrCreateMaterial("MAT_BARE_CABLE_CLIP", "Клипса для открытой прокладки", "шт", CATEGORY_ROUTE_OPEN));
        materials.put(
                "MAT_CABLE_TIE",
                getOrCreateMaterial("MAT_CABLE_TIE", "Стяжка для кабеля", "шт", CATEGORY_ROUTE_OPEN));
        return materials;
    }

    private Material getOrCreateMaterial(String code, String name, String unit, String category) {
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        Material material = materialRepository
                .findByCode(normalizedCode)
                .orElseGet(
                        () -> {
                            Material m = new Material();
                            m.setCode(normalizedCode);
                            return m;
                        });
        material.setName(name);
        material.setUnit(unit);
        material.setCategory(category);
        return materialRepository.save(material);
    }

    private void createDefaultNorms(Map<String, Material> materials) {
        createNormIfMissing(
                "ENDPOINT_CAMERA_FIXING", materials.get("MAT_CAMERA_ANCHOR"), "4", "4 точки крепления на камеру");
        createNormIfMissing(
                "ENDPOINT_CAMERA_RJ45", materials.get("MAT_RJ45_PLUG"), "2", "2 коннектора RJ-45 на камеру");
        createNormIfMissing(
                "ENDPOINT_ACCESS_POINT_FIXING", materials.get("MAT_ACCESS_POINT_ANCHOR"), "4",
                "4 точки крепления на точку доступа");
        createNormIfMissing(
                "ENDPOINT_ACCESS_POINT_RJ45", materials.get("MAT_RJ45_PLUG"), "2", "2 коннектора RJ-45 на точку доступа");
        createNormIfMissing(
                "ENDPOINT_NETWORK_OUTLET_FIXING", materials.get("MAT_READER_ANCHOR"), "4", "Крепёж розетки");
        createNormIfMissing(
                "ENDPOINT_NETWORK_OUTLET_RJ45", materials.get("MAT_RJ45_PLUG"), "2", "2 коннектора RJ-45 на розетку");
        createNormIfMissing(
                "ENDPOINT_READER_FIXING", materials.get("MAT_READER_ANCHOR"), "4", "4 точки крепления на считыватель");
        createNormIfMissing(
                "ENDPOINT_READER_RJ45", materials.get("MAT_RJ45_PLUG"), "2", "2 коннектора RJ-45 на считыватель");
        createNormIfMissing(
                "ENDPOINT_TURNSTILE_FIXING", materials.get("MAT_TURNSTILE_ANCHOR"), "4", "Базовый крепёж турникета");
        createNormIfMissing(
                "ENDPOINT_OTHER_NETWORK_DEVICE_FIXING", materials.get("MAT_ACCESS_POINT_ANCHOR"), "4",
                "Крепёж сетевого устройства");
        createNormIfMissing(
                "ENDPOINT_OTHER_NETWORK_DEVICE_RJ45", materials.get("MAT_RJ45_PLUG"), "2",
                "Коннекторы на сетевое устройство");
        createNormIfMissing(
                "NODE_CABINET_FIXING_WALL", materials.get("MAT_CABINET_ANCHOR"), "4",
                "Шкаф на 4 анкера при монтаже на стену");
        createNormIfMissing(
                "NODE_INPUT_GLAND", materials.get("MAT_INPUT_GLAND"), "incomingLinesCount",
                "Количество муфт соответствует количеству приходящих линий");
        createNormIfMissing(
                "NODE_LUGS", materials.get("MAT_POWER_LUG"), "lugCount", "Набор наконечников силовых, временная формула");
        createNormIfMissing(
                "NODE_SOCKET_DOUBLE", materials.get("MAT_SOCKET_DOUBLE"), "baseSockets + extraSockets",
                "Количество двойных розеток: базовые + доп.");
        createNormIfMissing(
                "NODE_CIRCUIT_BREAKER", materials.get("MAT_BREAKER"), "baseBreakers + extraBreakers",
                "Количество автоматов: базовые + доп.");
        createNormIfMissing(
                "NODE_CABINET_400", materials.get("MAT_CABINET_400"), "1", "Один шкаф 400 мм на узел");
        createNormIfMissing(
                "LINK_UTP_LENGTH", materials.get("CABLE_UTP_CAT5E"), "length", "Длина UTP-линии в метрах");
        createNormIfMissing(
                "LINK_POWER_LENGTH", materials.get("CABLE_POWER_3X1_5"), "length", "Длина силовой линии в метрах");
        createNormIfMissing(
                "FIBER_4", materials.get("CABLE_FIBER_4"), "length", "Оптический кабель 4 волокна по длине");
        createNormIfMissing(
                "FIBER_8", materials.get("CABLE_FIBER_8"), "length", "Оптический кабель 8 волокон по длине");
        createNormIfMissing(
                "CORRUGATED_PIPE", materials.get("MAT_CORRUGATED_PIPE_20"), "length",
                "Длина гофры по трассе");
        createNormIfMissing(
                "CORRUGATED_PIPE_HORIZONTAL_CLIP", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы для горизонтальной гофры с шагом по настройкам");
        createNormIfMissing(
                "CORRUGATED_PIPE_VERTICAL_CLIP", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы для вертикальной гофры с шагом по настройкам");
        createNormIfMissing(
                "CORRUGATED_PIPE_COUPLING", materials.get("MAT_CORRUGATED_COUPLING"), "length / 10",
                "Муфты через каждые 10 метров");
        createNormIfMissing(
                "CORRUGATED_PIPE_BRANCH", materials.get("MAT_CORRUGATED_BRANCH"), "branchCount",
                "Ответвления по количеству линий");
        createNormIfMissing(
                "CABLE_CHANNEL", materials.get("MAT_CABLE_CHANNEL_40X25"), "length", "Кабель-канал по длине трассы");
        createNormIfMissing(
                "CABLE_CHANNEL_CLIP", materials.get("MAT_CABLE_CHANNEL_CLIP"), "length / step",
                "Клипсы для кабель-канала");
        createNormIfMissing(
                "CABLE_CHANNEL_FASTENER", materials.get("MAT_CABLE_CHANNEL_FASTENER"), "length / step",
                "Базовый крепёж кабель-канала");
        createNormIfMissing(
                "TRAY_OR_STRUCTURE", materials.get("MAT_CABLE_TRAY_100"), "length", "Лоток/конструкция по длине");
        createNormIfMissing(
                "TRAY_OR_STRUCTURE_TIES", materials.get("MAT_TRAY_TIE"), "length / step",
                "Стяжки для лотков с шагом 0.5 м");
        createNormIfMissing(
                "WIRE_ROPE", materials.get("MAT_WIRE_ROPE_4MM"), "length", "Несущий трос по длине");
        createNormIfMissing(
                "WIRE_ROPE_ANCHOR", materials.get("MAT_WIRE_ROPE_ANCHOR"), "length / step",
                "Анкера для троса каждые 0.3 м");
        createNormIfMissing(
                "WIRE_ROPE_TURNBUCKLE", materials.get("MAT_WIRE_ROPE_TURNBUCKLE"), "length / step",
                "Талрепы по шагу троса");
        createNormIfMissing(
                "WIRE_ROPE_CLAMP", materials.get("MAT_WIRE_ROPE_CLAMP"), "length / step",
                "Зажимы для троса по шагу 0.3 м");
        createNormIfMissing(
                "BARE_CABLE", materials.get("MAT_BARE_CABLE_CLIP"), "length / step", "Открытая прокладка — базовый материал");
        createNormIfMissing(
                "BARE_CABLE_ONE_CLIP", materials.get("MAT_BARE_CABLE_CLIP"), "length / step",
                "Клипсы для открытой прокладки");
        createNormIfMissing(
                "BARE_CABLE_PE_TIES", materials.get("MAT_CABLE_TIE"), "length / step",
                "Стяжки для открытой прокладки");
        createNormIfMissing(
                "CABLE_CHANNEL_FASTENER_BETON_OR_BRICK", materials.get("MAT_CABLE_CHANNEL_FASTENER"), "length / step",
                "Крепёж кабель-канала для бетона/кирпича");
        createNormIfMissing(
                "CABLE_CHANNEL_FASTENER_METAL", materials.get("MAT_CABLE_CHANNEL_FASTENER"), "length / step",
                "Крепёж кабель-канала для металла");
        createNormIfMissing(
                "CABLE_CHANNEL_FASTENER_WOOD", materials.get("MAT_CABLE_CHANNEL_FASTENER"), "length / step",
                "Крепёж кабель-канала для дерева");
        createNormIfMissing(
                "CABLE_CHANNEL_FASTENER_GYPSUM", materials.get("MAT_CABLE_CHANNEL_FASTENER"), "length / step",
                "Крепёж кабель-канала для гипсокартона");
        createNormIfMissing(
                "CORRUGATED_PIPE_HORIZONTAL_CLIP_BETON_OR_BRICK", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы гофры по бетону/кирпичу");
        createNormIfMissing(
                "CORRUGATED_PIPE_HORIZONTAL_CLIP_METAL", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы гофры по металлу");
        createNormIfMissing(
                "CORRUGATED_PIPE_HORIZONTAL_CLIP_WOOD", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы гофры по дереву");
        createNormIfMissing(
                "CORRUGATED_PIPE_HORIZONTAL_CLIP_GYPSUM", materials.get("MAT_CORRUGATED_CLIP"), "length / step",
                "Клипсы гофры по гипсокартону");
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
