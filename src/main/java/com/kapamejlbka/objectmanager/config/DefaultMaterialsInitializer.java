package com.kapamejlbka.objectmanager.config;

import com.kapamejlbka.objectmanager.domain.material.Material;
import com.kapamejlbka.objectmanager.domain.material.MaterialCategory;
import com.kapamejlbka.objectmanager.domain.material.MaterialNorm;
import com.kapamejlbka.objectmanager.domain.material.MaterialNormContext;
import com.kapamejlbka.objectmanager.repository.MaterialNormRepository;
import com.kapamejlbka.objectmanager.repository.MaterialRepository;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultMaterialsInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMaterialsInitializer.class);

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
        Map<String, Material> materials = materialRepository.count() == 0
                ? createDefaultMaterials()
                : loadExistingMaterials();

        if (materialNormRepository.count() == 0) {
            LOG.info("Seeding default material norms");
            createDefaultNorms(materials);
        }
    }

    private Map<String, Material> createDefaultMaterials() {
        Map<String, Material> materials = new LinkedHashMap<>();
        addMaterial(materials, "CABLE_UTP_CAT5E", "Кабель UTP Cat.5e", "м", MaterialCategory.CABLE_UTP);
        addMaterial(materials, "CABLE_UTP_CAT6", "Кабель UTP Cat.6", "м", MaterialCategory.CABLE_UTP);

        addMaterial(materials, "CABLE_POWER_3X1_5", "Кабель питания 3×1,5", "м", MaterialCategory.CABLE_POWER);
        addMaterial(materials, "CABLE_POWER_3X2_5", "Кабель питания 3×2,5", "м", MaterialCategory.CABLE_POWER);

        addMaterial(materials, "CABLE_FIBER_4", "Оптический кабель 4 волокна", "м", MaterialCategory.CABLE_FIBER);
        addMaterial(materials, "CABLE_FIBER_8", "Оптический кабель 8 волокон", "м", MaterialCategory.CABLE_FIBER);

        addMaterial(materials, "PIPE_CORRUGATED_16", "Гофра 16 мм", "м", MaterialCategory.PIPE_CORRUGATED);
        addMaterial(materials, "PIPE_CORRUGATED_20", "Гофра 20 мм", "м", MaterialCategory.PIPE_CORRUGATED);
        addMaterial(materials, "PIPE_CORRUGATED_25", "Гофра 25 мм", "м", MaterialCategory.PIPE_CORRUGATED);

        addMaterial(materials, "CABLE_CHANNEL_25X16", "Кабель-канал 25×16", "м", MaterialCategory.CABLE_CHANNEL);
        addMaterial(materials, "CABLE_CHANNEL_40X25", "Кабель-канал 40×25", "м", MaterialCategory.CABLE_CHANNEL);
        addMaterial(materials, "CABLE_CHANNEL_60X40", "Кабель-канал 60×40", "м", MaterialCategory.CABLE_CHANNEL);

        addMaterial(materials, "WIRE_ROPE_4", "Трос 4 мм", "м", MaterialCategory.WIRE_ROPE);
        addMaterial(
                materials,
                "WIRE_ROPE_ANCHOR_M8",
                "Анкер-кольцо M8",
                "шт",
                MaterialCategory.FASTENER_WIRE_ROPE);
        addMaterial(
                materials,
                "WIRE_ROPE_TURNBUCKLE_M8",
                "Талреп M8",
                "шт",
                MaterialCategory.FASTENER_WIRE_ROPE);
        addMaterial(
                materials,
                "WIRE_ROPE_CLAMP_4",
                "Зажим троса 4 мм",
                "шт",
                MaterialCategory.FASTENER_WIRE_ROPE);

        addMaterial(materials, "CLIP_PIPE_16", "Клипса для гофры 16 мм", "шт", MaterialCategory.FASTENER_CLIP);
        addMaterial(materials, "CLIP_PIPE_20", "Клипса для гофры 20 мм", "шт", MaterialCategory.FASTENER_CLIP);
        addMaterial(materials, "CLIP_PIPE_25", "Клипса для гофры 25 мм", "шт", MaterialCategory.FASTENER_CLIP);

        addMaterial(materials, "DOWEL_6X40", "Дюбель 6×40", "шт", MaterialCategory.FASTENER_DOWEL);
        addMaterial(materials, "SCREW_4_2X50", "Саморез 4,2×50", "шт", MaterialCategory.FASTENER_SCREW);
        addMaterial(materials, "TIE_3_6X200", "Стяжка 3,6×200", "шт", MaterialCategory.FASTENER_TIE);

        addMaterial(materials, "BOX_JUNCTION_SMALL", "Распределительная коробка малая", "шт", MaterialCategory.BOX);

        addMaterial(materials, "CABINET_350", "Шкаф навесной 350 мм", "шт", MaterialCategory.CABINET);
        addMaterial(materials, "CABINET_400", "Шкаф навесной 400 мм", "шт", MaterialCategory.CABINET);
        addMaterial(materials, "CABINET_500", "Шкаф навесной 500 мм", "шт", MaterialCategory.CABINET);

        addMaterial(materials, "RJ45_PLUG", "Разъём RJ-45", "шт", MaterialCategory.CONNECTOR_RJ45);
        addMaterial(materials, "SOCKET_DOUBLE", "Розетка двойная 220 В", "шт", MaterialCategory.ELECTRIC_SOCKET);
        addMaterial(materials, "BREAKER_B16", "Автомат 16A", "шт", MaterialCategory.ELECTRIC_BREAKER);
        addMaterial(materials, "LUG_POWER_SMALL", "Наконечник силовой", "шт", MaterialCategory.ELECTRIC_LUG);

        addMaterial(materials, "INPUT_GLAND", "Вводная муфта", "шт", MaterialCategory.OTHER);
        addMaterial(materials, "FIBER_SPLICE_PROTECTOR", "Защита оптической сварки", "шт", MaterialCategory.CABLE_FIBER);
        addMaterial(materials, "FIBER_CONNECTOR", "Оптический коннектор", "шт", MaterialCategory.CABLE_FIBER);

        return materials;
    }

    private void addMaterial(
            Map<String, Material> materials, String code, String name, String unit, MaterialCategory category) {
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        Material material = new Material();
        material.setCode(normalizedCode);
        material.setName(name);
        material.setUnit(unit);
        material.setCategory(category);
        materials.put(normalizedCode, materialRepository.save(material));
    }

    private void createDefaultNorms(Map<String, Material> materials) {
        addNorm(materials, MaterialNormContext.ENDPOINT_CAMERA_FIXING, "DOWEL_6X40", "4", "4 точки крепления на камеру");
        addNorm(materials, MaterialNormContext.ENDPOINT_CAMERA_FIXING, "SCREW_4_2X50", "4", "4 точки крепления на камеру");
        addNorm(materials, MaterialNormContext.ENDPOINT_CAMERA_RJ45, "RJ45_PLUG", "2", "2 разъёма RJ-45 на камеру");

        addNorm(materials, MaterialNormContext.ENDPOINT_READER_FIXING, "DOWEL_6X40", "4", "Крепёж считывателя");
        addNorm(materials, MaterialNormContext.ENDPOINT_READER_FIXING, "SCREW_4_2X50", "4", "Крепёж считывателя");
        addNorm(materials, MaterialNormContext.ENDPOINT_READER_RJ45, "RJ45_PLUG", "2", "Разъёмы RJ-45 на считыватель");

        addNorm(
                materials,
                MaterialNormContext.ENDPOINT_ACCESS_POINT_FIXING,
                "DOWEL_6X40",
                "4",
                "4 точки крепления точки доступа");
        addNorm(materials, MaterialNormContext.ENDPOINT_ACCESS_POINT_FIXING, "SCREW_4_2X50", "4", "4 точки крепления точки доступа");
        addNorm(materials, MaterialNormContext.ENDPOINT_ACCESS_POINT_RJ45, "RJ45_PLUG", "4", "Разъёмы точки доступа");

        addNorm(materials, MaterialNormContext.ENDPOINT_NETWORK_OUTLET_FIXING, "CLIP_PIPE_16", "4", "Крепёж розетки");
        addNorm(materials, MaterialNormContext.ENDPOINT_NETWORK_OUTLET_RJ45, "RJ45_PLUG", "2", "Разъёмы на розетку");

        addNorm(materials, MaterialNormContext.ENDPOINT_TURNSTILE_FIXING, "CLIP_PIPE_25", "4", "Крепёж турникета");
        addNorm(
                materials,
                MaterialNormContext.ENDPOINT_OTHER_NETWORK_DEVICE_FIXING,
                "DOWEL_6X40",
                "4",
                "Крепёж сетевого устройства");
        addNorm(materials, MaterialNormContext.ENDPOINT_OTHER_NETWORK_DEVICE_FIXING, "SCREW_4_2X50", "4", "Крепёж сетевого устройства");

        addNorm(materials, MaterialNormContext.NODE_CABINET_FIXING_WALL, "WIRE_ROPE_ANCHOR_M8", "4", "Анкера под шкаф");
        addNorm(materials, MaterialNormContext.NODE_CABINET_FIXING_CEILING, "WIRE_ROPE_ANCHOR_M8", "4", "Крепёж шкафа к потолку");
        addNorm(materials, MaterialNormContext.NODE_CABINET_FIXING_POLE, "WIRE_ROPE_ANCHOR_M8", "4", "Крепёж шкафа к опоре");
        addNorm(materials, MaterialNormContext.NODE_CABINET_FIXING_RACK, "WIRE_ROPE_ANCHOR_M8", "4", "Крепёж шкафа к стойке");
        addNorm(
                materials,
                MaterialNormContext.NODE_INPUT_GLAND,
                "INPUT_GLAND",
                "incomingLinesCount",
                "Вводные муфты по количеству линий");
        addNorm(materials, MaterialNormContext.NODE_LUGS, "LUG_POWER_SMALL", "10 + 4 * extraSockets", "Набор силовых наконечников");
        addNorm(materials, MaterialNormContext.NODE_SOCKET_DOUBLE, "SOCKET_DOUBLE", "1", "Базовая розетка 220 В");
        addNorm(materials, MaterialNormContext.NODE_CIRCUIT_BREAKER, "BREAKER_B16", "1", "Базовый автомат");
        addNorm(materials, MaterialNormContext.NODE_CABINET_350, "CABINET_350", "1", "Шкаф 350 мм на узел");
        addNorm(materials, MaterialNormContext.NODE_CABINET_400, "CABINET_400", "1", "Шкаф 400 мм на узел");
        addNorm(materials, MaterialNormContext.NODE_CABINET_500, "CABINET_500", "1", "Шкаф 500 мм на узел");

        addNorm(materials, MaterialNormContext.CORRUGATED_PIPE, "PIPE_CORRUGATED_20", "length", "Гофра по длине трассы");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_HORIZONTAL_CLIP,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Клипсы гофры горизонтально");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_VERTICAL_CLIP,
                "CLIP_PIPE_20",
                "ceil(length / 0.5)",
                "Клипсы гофры вертикально");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_HORIZONTAL_CLIP_BETON_OR_BRICK,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Клипсы по бетону/кирпичу");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_HORIZONTAL_CLIP_METAL,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Клипсы по металлу");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_HORIZONTAL_CLIP_WOOD,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Клипсы по дереву");
        addNorm(
                materials,
                MaterialNormContext.CORRUGATED_PIPE_HORIZONTAL_CLIP_GYPSUM,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Клипсы по гипсокартону");

        addNorm(materials, MaterialNormContext.CABLE_CHANNEL, "CABLE_CHANNEL_40X25", "length", "Кабель-канал по длине");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER,
                "DOWEL_6X40",
                "ceil(length / 0.4)",
                "Дюбели для кабель-канала");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER,
                "SCREW_4_2X50",
                "ceil(length / 0.4)",
                "Саморезы для кабель-канала");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER_BETON_OR_BRICK,
                "DOWEL_6X40",
                "ceil(length / 0.4)",
                "Дюбели по бетону/кирпичу");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER_METAL,
                "SCREW_4_2X50",
                "ceil(length / 0.4)",
                "Крепёж по металлу");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER_WOOD,
                "SCREW_4_2X50",
                "ceil(length / 0.4)",
                "Крепёж по дереву");
        addNorm(
                materials,
                MaterialNormContext.CABLE_CHANNEL_FASTENER_GYPSUM,
                "DOWEL_6X40",
                "ceil(length / 0.4)",
                "Крепёж по гипсокартону");

        addNorm(materials, MaterialNormContext.WIRE_ROPE, "WIRE_ROPE_4", "length", "Трос по длине трассы");
        addNorm(materials, MaterialNormContext.WIRE_ROPE_ANCHOR, "WIRE_ROPE_ANCHOR_M8", "2", "Анкера на трассу");
        addNorm(materials, MaterialNormContext.WIRE_ROPE_TURNBUCKLE, "WIRE_ROPE_TURNBUCKLE_M8", "2", "Талрепы на трассу");
        addNorm(materials, MaterialNormContext.WIRE_ROPE_CLAMP, "WIRE_ROPE_CLAMP_4", "4", "Зажимы на трассу");

        addNorm(materials, MaterialNormContext.BARE_CABLE, "CABLE_POWER_3X1_5", "length", "Открытая прокладка");
        addNorm(
                materials,
                MaterialNormContext.BARE_CABLE_ONE_CLIP,
                "CLIP_PIPE_16",
                "ceil(length / 0.4)",
                "Клипсы для открытой прокладки");
        addNorm(
                materials,
                MaterialNormContext.BARE_CABLE_PE_TIES,
                "TIE_3_6X200",
                "ceil(length / 0.4)",
                "Стяжки для открытой прокладки");

        addNorm(
                materials,
                MaterialNormContext.ROUTE_CORRUGATED_HORIZONTAL_CLIP,
                "CLIP_PIPE_20",
                "ceil(length / 0.4)",
                "Горизонтальная гофра — клипсы");
        addNorm(
                materials,
                MaterialNormContext.ROUTE_CORRUGATED_VERTICAL_CLIP,
                "CLIP_PIPE_20",
                "ceil(length / 0.5)",
                "Вертикальная гофра — клипсы");
        addNorm(
                materials,
                MaterialNormContext.ROUTE_CORRUGATED_ON_WIRE_ROPE,
                "TIE_3_6X200",
                "ceil(length / 0.3)",
                "Гофра по тросу — стяжки");
        addNorm(
                materials,
                MaterialNormContext.ROUTE_CABLE_CHANNEL_FIXING,
                "DOWEL_6X40",
                "ceil(length / 0.4)",
                "Крепёж кабель-канала");
        addNorm(
                materials,
                MaterialNormContext.ROUTE_CABLE_CHANNEL_FIXING,
                "SCREW_4_2X50",
                "ceil(length / 0.4)",
                "Саморезы для кабель-канала");
        addNorm(
                materials,
                MaterialNormContext.ROUTE_BARE_CABLE_CLIP,
                "CLIP_PIPE_16",
                "ceil(length / 0.4)",
                "Клипсы открытой прокладки");
        addNorm(materials, MaterialNormContext.ROUTE_WIRE_ROPE_ANCHOR, "WIRE_ROPE_ANCHOR_M8", "2", "Анкера троса");
        addNorm(materials, MaterialNormContext.ROUTE_WIRE_ROPE_TURNBUCKLE, "WIRE_ROPE_TURNBUCKLE_M8", "2", "Талрепы троса");
        addNorm(materials, MaterialNormContext.ROUTE_WIRE_ROPE_CLAMP, "WIRE_ROPE_CLAMP_4", "4", "Зажимы троса");

        addNorm(materials, MaterialNormContext.LINK_UTP_LENGTH, "CABLE_UTP_CAT5E", "length", "Длина линии UTP");
        addNorm(materials, MaterialNormContext.LINK_POWER_LENGTH, "CABLE_POWER_3X1_5", "length", "Длина силовой линии");
        addNorm(materials, MaterialNormContext.FIBER_4, "CABLE_FIBER_4", "length", "Оптика 4 волокна по длине");
        addNorm(materials, MaterialNormContext.FIBER_8, "CABLE_FIBER_8", "length", "Оптика 8 волокон по длине");
        addNorm(materials, MaterialNormContext.LINK_FIBER_4_LENGTH, "CABLE_FIBER_4", "length", "Оптика 4 волокна");
        addNorm(materials, MaterialNormContext.LINK_FIBER_8_LENGTH, "CABLE_FIBER_8", "length", "Оптика 8 волокон");
        addNorm(materials, MaterialNormContext.FIBER_SPLICE, "FIBER_SPLICE_PROTECTOR", "fiberSpliceCount", "Защита сварок");
        addNorm(
                materials,
                MaterialNormContext.FIBER_CONNECTOR,
                "FIBER_CONNECTOR",
                "fiberConnectorCount",
                "Коннекторы оптики");
    }

    private Map<String, Material> loadExistingMaterials() {
        return materialRepository.findAll().stream()
                .collect(
                        Collectors.toMap(
                                material -> material.getCode().toUpperCase(Locale.ROOT),
                                Function.identity(),
                                (first, second) -> first,
                                LinkedHashMap::new));
    }

    private void addNorm(
            Map<String, Material> materials,
            MaterialNormContext context,
            String materialCode,
            String formula,
            String description) {
        Material material = materials.get(materialCode.toUpperCase(Locale.ROOT));
        if (material == null) {
            LOG.warn("Material {} is missing for default norm {}", materialCode, context.name());
            return;
        }
        MaterialNorm norm = new MaterialNorm();
        norm.setContextType(context);
        norm.setMaterial(material);
        norm.setFormula(formula);
        norm.setDescription(description);
        materialNormRepository.save(norm);
    }
}
