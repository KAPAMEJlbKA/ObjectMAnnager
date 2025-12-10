package com.kapamejlbka.objectmanager.domain.material;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum MaterialNormContext {
    CORRUGATED_PIPE("Гофрированная труба — основной материал"),
    CORRUGATED_PIPE_HORIZONTAL_CLIP("Гофра на стене — клипсы (горизонталь)"),
    CORRUGATED_PIPE_VERTICAL_CLIP("Гофра на стене — клипсы (вертикаль)"),
    CORRUGATED_PIPE_COUPLING("Гофра — соединительные муфты"),
    CORRUGATED_PIPE_BRANCH("Гофра — ответвления"),
    CORRUGATED_PIPE_HORIZONTAL_CLIP_BETON_OR_BRICK("Гофра по бетону/кирпичу — клипсы"),
    CORRUGATED_PIPE_HORIZONTAL_CLIP_METAL("Гофра по металлу — клипсы"),
    CORRUGATED_PIPE_HORIZONTAL_CLIP_WOOD("Гофра по дереву — клипсы"),
    CORRUGATED_PIPE_HORIZONTAL_CLIP_GYPSUM("Гофра по гипсокартону — клипсы"),

    CABLE_CHANNEL("Кабель-канал — основной материал"),
    CABLE_CHANNEL_CLIP("Кабель-канал — клипсы"),
    CABLE_CHANNEL_FASTENER("Кабель-канал — крепёж"),
    CABLE_CHANNEL_FASTENER_BETON_OR_BRICK("Кабель-канал — крепёж для бетона/кирпича"),
    CABLE_CHANNEL_FASTENER_METAL("Кабель-канал — крепёж для металла"),
    CABLE_CHANNEL_FASTENER_WOOD("Кабель-канал — крепёж для дерева"),
    CABLE_CHANNEL_FASTENER_GYPSUM("Кабель-канал — крепёж для гипсокартона"),

    TRAY_OR_STRUCTURE("Лоток/конструкция — основной материал"),
    TRAY_OR_STRUCTURE_TIES("Лоток/конструкция — стяжки"),

    WIRE_ROPE("Трос — основной материал"),
    WIRE_ROPE_ANCHOR("Трос — анкера"),
    WIRE_ROPE_TURNBUCKLE("Трос — талрепы"),
    WIRE_ROPE_CLAMP("Трос — зажимы"),

    BARE_CABLE("Открытая прокладка — основной материал"),
    BARE_CABLE_ONE_CLIP("Открытая прокладка — крепёж (одна клипса)"),
    BARE_CABLE_PE_TIES("Открытая прокладка — стяжки"),

    ENDPOINT_CAMERA_FIXING("Видеокамера — крепёж"),
    ENDPOINT_CAMERA_RJ45("Видеокамера — разъёмы RJ-45"),
    ENDPOINT_ACCESS_POINT_FIXING("Точка доступа — крепёж"),
    ENDPOINT_ACCESS_POINT_RJ45("Точка доступа — разъёмы RJ-45"),
    ENDPOINT_NETWORK_OUTLET_FIXING("Сетевая розетка — крепёж"),
    ENDPOINT_NETWORK_OUTLET_RJ45("Сетевая розетка — разъёмы RJ-45"),
    ENDPOINT_READER_FIXING("Считыватель — крепёж"),
    ENDPOINT_READER_RJ45("Считыватель — разъёмы RJ-45"),
    ENDPOINT_TURNSTILE_FIXING("Турникет — крепёж"),
    ENDPOINT_OTHER_NETWORK_DEVICE_FIXING("Сетевое устройство — крепёж"),
    ENDPOINT_OTHER_NETWORK_DEVICE_RJ45("Сетевое устройство — разъёмы RJ-45"),

    NODE_CABINET_FIXING("Шкаф — крепёж"),
    NODE_CABINET_FIXING_WALL("Шкаф — крепёж к стене"),
    NODE_CABINET_FIXING_CEILING("Шкаф — крепёж к потолку"),
    NODE_CABINET_FIXING_POLE("Шкаф — крепёж к опоре"),
    NODE_CABINET_FIXING_RACK("Шкаф — крепёж к стойке/шкафу"),
    NODE_INPUT_GLAND("Шкаф — вводные муфты"),
    NODE_LUGS("Шкаф — наконечники"),
    NODE_SOCKET_DOUBLE("Шкаф — розетки 220 В"),
    NODE_CIRCUIT_BREAKER("Шкаф — автоматы"),
    NODE_CABINET_350("Шкаф 350 мм"),
    NODE_CABINET_400("Шкаф 400 мм"),
    NODE_CABINET_500("Шкаф 500 мм"),

    LINK_UTP_LENGTH("Кабель UTP — по длине линии"),
    LINK_POWER_LENGTH("Силовой кабель — по длине линии"),
    FIBER_4("Оптический кабель 4 волокна — по длине линии"),
    FIBER_8("Оптический кабель 8 волокон — по длине линии"),
    FIBER_SPLICE("Оптическая линия — сварки"),
    FIBER_CONNECTOR("Оптическая линия — коннекторы"),

    ROUTE_CORRUGATED_HORIZONTAL_CLIP("Гофра на стене — клипсы (горизонталь)", "Route"),
    ROUTE_CORRUGATED_VERTICAL_CLIP("Гофра на стене — клипсы (вертикаль)", "Route"),
    ROUTE_CORRUGATED_ON_WIRE_ROPE("Гофра по тросу — стяжки", "Route"),
    ROUTE_CABLE_CHANNEL_FIXING("Кабель-канал — крепёж", "Route"),
    ROUTE_BARE_CABLE_CLIP("Открытая прокладка — крепёж/клипсы", "Route"),
    ROUTE_WIRE_ROPE_ANCHOR("Трос — анкера", "Route"),
    ROUTE_WIRE_ROPE_TURNBUCKLE("Трос — талрепы", "Route"),
    ROUTE_WIRE_ROPE_CLAMP("Трос — зажимы", "Route"),

    LINK_FIBER_4_LENGTH("Оптический кабель 4 волокна — по длине линии", "Link"),
    LINK_FIBER_8_LENGTH("Оптический кабель 8 волокон — по длине линии", "Link");

    private final String displayNameRu;
    private final String group;

    MaterialNormContext(String displayNameRu) {
        this(displayNameRu, null);
    }

    MaterialNormContext(String displayNameRu, String group) {
        this.displayNameRu = displayNameRu;
        this.group = group;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }

    public String getGroup() {
        return group;
    }

    public static Map<String, String> availableContexts() {
        Map<String, String> names = new LinkedHashMap<>();
        for (MaterialNormContext context : values()) {
            names.put(context.name(), context.getDisplayNameRu());
        }
        return names;
    }

    public static List<MaterialNormContext> orderedValues() {
        return Arrays.asList(values());
    }

    public static String displayName(String contextType) {
        if (contextType == null || contextType.isBlank()) {
            return "";
        }
        try {
            return MaterialNormContext.valueOf(contextType).getDisplayNameRu();
        } catch (IllegalArgumentException ex) {
            return contextType;
        }
    }
}
