package com.kapamejlbka.objectmanager.domain.material;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MaterialNormContext {

    private static final Map<String, String> CONTEXT_NAMES = new LinkedHashMap<>();

    static {
        CONTEXT_NAMES.put("CORRUGATED_PIPE_HORIZONTAL_CLIP", "Гофра горизонтальная — клипсы");
        CONTEXT_NAMES.put("CORRUGATED_PIPE_VERTICAL_CLIP", "Гофра вертикальная — клипсы");
        CONTEXT_NAMES.put("CABLE_CHANNEL_DOWEL", "Кабель-канал — дюбели");
        CONTEXT_NAMES.put("WIRE_ROPE_TURNBUCKLE", "Трос — талреп");
        CONTEXT_NAMES.put("WIRE_ROPE_ANCHOR", "Трос — анкеры");
        CONTEXT_NAMES.put("BARE_CABLE_ONE_CLIP_FIXING", "Голый кабель — крепление одной клипсой");
        CONTEXT_NAMES.put("ENDPOINT_CAMERA_FIXING", "Видеокамера — крепление");
        CONTEXT_NAMES.put("ENDPOINT_CAMERA_RJ45", "Видеокамера — коннекторы RJ-45");
        CONTEXT_NAMES.put("ENDPOINT_ACCESS_POINT_RJ45", "Точка доступа — коннекторы RJ-45");
        CONTEXT_NAMES.put("NODE_CABINET_SCREW", "Узел/шкаф — крепёжные винты");
        CONTEXT_NAMES.put("NODE_CABINET_LUG", "Узел/шкаф — наконечники");
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
