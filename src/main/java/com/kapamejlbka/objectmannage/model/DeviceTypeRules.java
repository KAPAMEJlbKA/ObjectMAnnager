package com.kapamejlbka.objectmannage.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DeviceTypeRules {

    private static final Map<String, DeviceRequirements> RULES = new ConcurrentHashMap<>();
    private static final Map<CableFunction, String> FUNCTION_LABELS = new EnumMap<>(CableFunction.class);

    static {
        FUNCTION_LABELS.put(CableFunction.SIGNAL, "Сигнальный кабель");
        FUNCTION_LABELS.put(CableFunction.LOW_VOLTAGE_POWER, "Кабель для слаботочного питания");
        FUNCTION_LABELS.put(CableFunction.POWER, "Силовой кабель");
        FUNCTION_LABELS.put(CableFunction.UNKNOWN, "Без категории");

        register(List.of("видеокамера", "ip камера", "камера"),
                DeviceRequirements.builder()
                        .camera(true)
                        .requireViewingDepth(true)
                        .requireAccessorySelection(true)
                        .addCableRequirement(new CableRequirement(CableFunction.SIGNAL, FUNCTION_LABELS.get(CableFunction.SIGNAL)))
                        .build());

        register(List.of("точка доступа wifi", "точка доступа wi-fi", "точка доступа wi fi"),
                DeviceRequirements.builder()
                        .addCableRequirement(new CableRequirement(CableFunction.SIGNAL, FUNCTION_LABELS.get(CableFunction.SIGNAL)))
                        .addMaterial(new MaterialRequirement("Разъём RJ-45 (8P8C)", "шт", 2))
                        .addMaterial(new MaterialRequirement("Патч-корд UTP", "шт", 1))
                        .build());

        register(List.of("точка сети", "сетевая точка"),
                DeviceRequirements.builder()
                        .addCableRequirement(new CableRequirement(CableFunction.SIGNAL, FUNCTION_LABELS.get(CableFunction.SIGNAL)))
                        .addMaterial(new MaterialRequirement("Разъём RJ-45 (8P8C)", "шт", 1))
                        .addMaterial(new MaterialRequirement("Сетевая розетка", "шт", 1))
                        .build());

        register(List.of("контроллер считыватель", "контроллер считывателя", "контроллер доступа"),
                DeviceRequirements.builder()
                        .addCableRequirement(new CableRequirement(CableFunction.SIGNAL, FUNCTION_LABELS.get(CableFunction.SIGNAL)))
                        .addCableRequirement(new CableRequirement(CableFunction.LOW_VOLTAGE_POWER,
                                FUNCTION_LABELS.get(CableFunction.LOW_VOLTAGE_POWER)))
                        .addMaterial(new MaterialRequirement("Разъём RJ-45 (8P8C)", "шт", 2))
                        .addMaterial(new MaterialRequirement("Наконечник НШВ", "шт", 10))
                        .build());

        register(List.of("турникет"),
                DeviceRequirements.builder()
                        .addCableRequirement(new CableRequirement(CableFunction.SIGNAL, FUNCTION_LABELS.get(CableFunction.SIGNAL)))
                        .addCableRequirement(new CableRequirement(CableFunction.LOW_VOLTAGE_POWER,
                                FUNCTION_LABELS.get(CableFunction.LOW_VOLTAGE_POWER)))
                        .addMaterial(new MaterialRequirement("Разъём RJ-45 (8P8C)", "шт", 2))
                        .addMaterial(new MaterialRequirement("Наконечник НШВИ", "шт", 10))
                        .build());
    }

    private DeviceTypeRules() {
    }

    private static void register(List<String> aliases, DeviceRequirements requirements) {
        for (String alias : aliases) {
            RULES.put(normalize(alias), requirements);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("[^A-Za-zА-Яа-я0-9 ]", " ")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
        return normalized;
    }

    public static Optional<DeviceRequirements> lookup(String deviceTypeName) {
        if (deviceTypeName == null) {
            return Optional.empty();
        }
        String normalized = normalize(deviceTypeName);
        if (RULES.containsKey(normalized)) {
            return Optional.of(RULES.get(normalized));
        }
        return Optional.empty();
    }

    public static Map<CableFunction, String> getFunctionLabels() {
        return Collections.unmodifiableMap(FUNCTION_LABELS);
    }

    public static List<CableRequirement> requiredCables(String deviceTypeName) {
        return lookup(deviceTypeName)
                .map(DeviceRequirements::cableRequirements)
                .orElseGet(Collections::emptyList);
    }

    public static List<MaterialRequirement> materials(String deviceTypeName) {
        return lookup(deviceTypeName)
                .map(DeviceRequirements::materialRequirements)
                .orElseGet(Collections::emptyList);
    }

    public static boolean requiresViewingDepth(String deviceTypeName) {
        return lookup(deviceTypeName).map(DeviceRequirements::requireViewingDepth).orElse(false);
    }

    public static boolean requiresAccessorySelection(String deviceTypeName) {
        return lookup(deviceTypeName).map(DeviceRequirements::requireAccessorySelection).orElse(false);
    }

    public static boolean isCamera(String deviceTypeName) {
        return lookup(deviceTypeName).map(DeviceRequirements::camera).orElse(false);
    }

    public static final class DeviceRequirements {
        private final List<CableRequirement> cableRequirements;
        private final List<MaterialRequirement> materialRequirements;
        private final boolean camera;
        private final boolean requireViewingDepth;
        private final boolean requireAccessorySelection;

        private DeviceRequirements(Builder builder) {
            this.cableRequirements = Collections.unmodifiableList(new ArrayList<>(builder.cableRequirements));
            this.materialRequirements = Collections.unmodifiableList(new ArrayList<>(builder.materialRequirements));
            this.camera = builder.camera;
            this.requireViewingDepth = builder.requireViewingDepth;
            this.requireAccessorySelection = builder.requireAccessorySelection;
        }

        public List<CableRequirement> cableRequirements() {
            return cableRequirements;
        }

        public List<MaterialRequirement> materialRequirements() {
            return materialRequirements;
        }

        public boolean camera() {
            return camera;
        }

        public boolean requireViewingDepth() {
            return requireViewingDepth;
        }

        public boolean requireAccessorySelection() {
            return requireAccessorySelection;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final List<CableRequirement> cableRequirements = new ArrayList<>();
            private final List<MaterialRequirement> materialRequirements = new ArrayList<>();
            private boolean camera;
            private boolean requireViewingDepth;
            private boolean requireAccessorySelection;

            private Builder() {
            }

            public Builder addCableRequirement(CableRequirement requirement) {
                if (requirement != null) {
                    cableRequirements.add(requirement);
                }
                return this;
            }

            public Builder addMaterial(MaterialRequirement requirement) {
                if (requirement != null) {
                    materialRequirements.add(requirement);
                }
                return this;
            }

            public Builder camera(boolean value) {
                this.camera = value;
                return this;
            }

            public Builder requireViewingDepth(boolean value) {
                this.requireViewingDepth = value;
                return this;
            }

            public Builder requireAccessorySelection(boolean value) {
                this.requireAccessorySelection = value;
                return this;
            }

            public DeviceRequirements build() {
                return new DeviceRequirements(this);
            }
        }
    }

    public static final class CableRequirement {
        private final CableFunction function;
        private final String label;

        public CableRequirement(CableFunction function, String label) {
            this.function = function;
            this.label = label;
        }

        public CableFunction function() {
            return function;
        }

        public String label() {
            return label;
        }
    }

    public static final class MaterialRequirement {
        private final String name;
        private final String unit;
        private final int quantityPerDevice;

        public MaterialRequirement(String name, String unit, int quantityPerDevice) {
            this.name = name;
            this.unit = unit;
            this.quantityPerDevice = quantityPerDevice;
        }

        public String name() {
            return name;
        }

        public String unit() {
            return unit;
        }

        public int quantityPerDevice() {
            return quantityPerDevice;
        }
    }

    public static String encodeFunctions(List<CableRequirement> requirements) {
        return requirements.stream()
                .map(requirement -> requirement.function() != null ? requirement.function().name() : "")
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(","));
    }
}
