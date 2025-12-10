package com.kapamejlbka.objectmanager.domain.material;

public enum MaterialCategory {
    CABLE_UTP("Слаботочный кабель UTP"),
    CABLE_POWER("Силовой кабель"),
    CABLE_FIBER("Оптоволоконный кабель"),

    PIPE_CORRUGATED("Гофрированная труба"),
    CABLE_CHANNEL("Кабель-канал"),
    WIRE_ROPE("Трос"),

    FASTENER_CLIP("Клипсы"),
    FASTENER_DOWEL("Дюбели"),
    FASTENER_SCREW("Саморезы"),
    FASTENER_TIE("Пластиковые стяжки"),
    FASTENER_WIRE_ROPE("Фурнитура троса"),

    BOX("Коробки"),
    CABINET("Шкафы"),
    CONNECTOR_RJ45("Разъёмы RJ-45"),

    ELECTRIC_BREAKER("Автоматы"),
    ELECTRIC_SOCKET("Розетки"),
    ELECTRIC_LUG("Наконечники"),

    OTHER("Прочее");

    private final String displayNameRu;

    MaterialCategory(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }
}
