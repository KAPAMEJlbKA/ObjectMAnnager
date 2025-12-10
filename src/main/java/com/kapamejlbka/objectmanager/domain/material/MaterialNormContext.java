package com.kapamejlbka.objectmanager.domain.material;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MaterialNormContext {

    private static final Map<String, String> CONTEXT_NAMES = new LinkedHashMap<>();

    static {
        CONTEXT_NAMES.put("CORRUGATED_PIPE_HORIZONTAL_CLIP", "Гофра горизонтальная — клипсы");
        CONTEXT_NAMES.put("CORRUGATED_PIPE_VERTICAL_CLIP", "Гофра вертикальная — клипсы");
        CONTEXT_NAMES.put("CORRUGATED_PIPE_COUPLING", "Гофра — соединительные муфты");
        CONTEXT_NAMES.put("CORRUGATED_PIPE_BRANCH", "Гофра — ответвления");
        CONTEXT_NAMES.put("CABLE_CHANNEL_CLIP", "Кабель-канал — клипсы");
        CONTEXT_NAMES.put("CABLE_CHANNEL_FASTENER", "Кабель-канал — крепёж");
        CONTEXT_NAMES.put("TRAY_OR_STRUCTURE_TIES", "Лоток/конструкция — стяжки");
        CONTEXT_NAMES.put("WIRE_ROPE_ANCHOR", "Трос — анкеры");
        CONTEXT_NAMES.put("WIRE_ROPE_TURNBUCKLE", "Трос — талреп");
        CONTEXT_NAMES.put("WIRE_ROPE_CLAMP", "Трос — зажимы");
        CONTEXT_NAMES.put("BARE_CABLE_ONE_CLIP", "Голый кабель — крепление одной клипсой");
        CONTEXT_NAMES.put("BARE_CABLE_PE_TIES", "Голый кабель — крепление стяжками");
        CONTEXT_NAMES.put("ENDPOINT_CAMERA_FIXING", "Видеокамера — крепление");
        CONTEXT_NAMES.put("ENDPOINT_CAMERA_RJ45", "Видеокамера — коннекторы RJ-45");
        CONTEXT_NAMES.put("ENDPOINT_ACCESS_POINT_FIXING", "Точка доступа — крепление");
        CONTEXT_NAMES.put("ENDPOINT_ACCESS_POINT_RJ45", "Точка доступа — коннекторы RJ-45");
        CONTEXT_NAMES.put("ENDPOINT_NETWORK_OUTLET_FIXING", "Розетка — крепление");
        CONTEXT_NAMES.put("ENDPOINT_NETWORK_OUTLET_RJ45", "Розетка — коннекторы RJ-45");
        CONTEXT_NAMES.put("ENDPOINT_READER_FIXING", "Считыватель — крепление");
        CONTEXT_NAMES.put("ENDPOINT_READER_RJ45", "Считыватель — коннекторы RJ-45");
        CONTEXT_NAMES.put("ENDPOINT_TURNSTILE_FIXING", "Турникет — крепление");
        CONTEXT_NAMES.put("ENDPOINT_OTHER_NETWORK_DEVICE_FIXING", "Другое сетевое устройство — крепление");
        CONTEXT_NAMES.put("ENDPOINT_OTHER_NETWORK_DEVICE_RJ45", "Другое сетевое устройство — коннекторы RJ-45");
        CONTEXT_NAMES.put("NODE_CABINET_FIXING", "Узел/шкаф — крепление");
        CONTEXT_NAMES.put("NODE_CABINET_FIXING_WALL", "Узел/шкаф — крепление на стену");
        CONTEXT_NAMES.put("NODE_INPUT_GLAND", "Узел/шкаф — вводные муфты");
        CONTEXT_NAMES.put("NODE_LUGS", "Узел/шкаф — силовые наконечники");
        CONTEXT_NAMES.put("NODE_SOCKET_DOUBLE", "Узел/шкаф — двойные розетки 220В");
        CONTEXT_NAMES.put("NODE_CIRCUIT_BREAKER", "Узел/шкаф — автоматические выключатели");
        CONTEXT_NAMES.put("NODE_CABINET_400", "Узел/шкаф — навесной шкаф 400мм");
        CONTEXT_NAMES.put("LINK_UTP_LENGTH", "Линия UTP — длина");
        CONTEXT_NAMES.put("LINK_POWER_LENGTH", "Силовая линия — длина");
        CONTEXT_NAMES.put("FIBER_4", "Оптический кабель на 4 волокна — длина");
        CONTEXT_NAMES.put("FIBER_8", "Оптический кабель на 8 волокон — длина");
        CONTEXT_NAMES.put("FIBER_SPLICE", "Оптическая линия — сварки");
        CONTEXT_NAMES.put("FIBER_CONNECTOR", "Оптическая линия — коннекторы");
    }

    private MaterialNormContext() {
    }

    public static Map<String, String> availableContexts() {
        return CONTEXT_NAMES;
    }

    public static String displayName(String contextType) {
        return CONTEXT_NAMES.getOrDefault(contextType, contextType);
    }
}
