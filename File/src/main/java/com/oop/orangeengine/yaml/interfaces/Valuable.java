package com.oop.orangeengine.yaml.interfaces;

import com.google.common.base.Preconditions;
import com.google.gson.internal.Primitives;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Valuable extends ConfigHolder, Sectionable {
    Map<String, ConfigValue> getHierarchyValues();

    default ConfigValue set(String path, Object object) {
        return set(path, object, true);
    }

    default ConfigValue set(String path, Object object, boolean splitAtDots) {
        if (path.contains(".") && splitAtDots) {
            String[] split = path.split("\\.");
            String valueName = split[split.length - 1];

            ConfigSection section = this instanceof ConfigSection ? (ConfigSection) this : null;
            for (String childName : Arrays.copyOf(split, split.length - 1)) {
                Optional<ConfigSection> child = getSection(childName);
                if (child.isPresent())
                    section = child.get();

                else {
                    section = new ConfigSection(childName, getConfig());
                    section.sections.put(childName, section);
                }
            }
            return section.set(valueName, object);

        } else {
            ConfigValue value;
            if (getValues().containsKey(path))
                value = getValues().get(path);
            else if (this instanceof Config)
                value = new ConfigValue(path, (Config) this);
            else
                value = new ConfigValue(path, ((ConfigSection) this));

            value.setObject(object);

            getValues().put(path, value);
            return value;
        }
    }

    Map<String, ConfigValue> getValues();

    default <T> T getAs(String path, Supplier<T> supplier) {
        Optional<ConfigValue> ocv = get(path);
        if (supplier == null && !ocv.isPresent())
            throw new IllegalStateException("Failed to find value in " + getConfig().getFile().getFileName() + " path: " + (this instanceof ConfigSection ? ((ConfigSection) this).getPath() + "." : "") + path);

        ConfigValue value = ocv.orElseGet(() -> set(path, supplier.get()));
        return (T) value.getObject();
    }

    default <T> T getAs(String path, Class<T> type) {
        Object object = getAs(path, (Supplier<T>) null);
        if (type == null)
            return (T) object;

        type = Primitives.wrap(type);
        if (type.isAssignableFrom(Primitives.unwrap(object.getClass())))
            return (T) object;

        if (type != Primitives.unwrap(object.getClass()))
            return (T) doConversion(object, type);

        return (T) object;
    }

    static <T> Object doConversion(Object parsed, Class<T> clazz) {
        clazz = Primitives.wrap(clazz);
        if (Primitives.wrap(parsed.getClass()) == clazz || clazz.isAssignableFrom(parsed.getClass()))
            return parsed;

        String value = parsed.toString();
        if (clazz == String.class) return value;

        if ((parsed.getClass() == Double.class || parsed.getClass() == Float.class && clazz == Integer.class)) {
            if (value.contains(".")) {
                String[] splitValue = value.split("\\.");
                int original = Integer.parseInt(splitValue[0]);
                int secondPart = splitValue[1].toCharArray()[0];

                if (secondPart > 4)
                    original += 1;

                value = Integer.toString(original);
            }
        }

        if (clazz == Integer.class)
            return Integer.valueOf(value);

        else if (clazz == Long.class)
            return Long.valueOf(value);

        else if (clazz == Float.class)
            return Float.valueOf(value);

        else if (clazz == Double.class)
            return Double.valueOf(value);

        else
            throw new IllegalStateException("Incorrect object type required: " + clazz.getSimpleName() + " found: " + parsed.getClass().getSimpleName());
    }

    default <T> T getAs(String path, Class<T> type, Supplier<T> supplier, String... comments) {
        Optional<ConfigValue> ocv = get(path);
        if (supplier == null && !ocv.isPresent())
            throw new IllegalStateException("Failed to find value in " + getConfig().getFile().getFileName() + " path: " + (this instanceof ConfigSection ? ((ConfigSection) this).getPath() + "." : "") + path);

        ConfigValue value = ocv.orElseGet(() -> {
            ConfigValue set = set(path, supplier.get());
            set.comments.addAll(Arrays.asList(comments));
            return set;
        });

        return (T) doConversion(value.getObject(), type);
    }

    default <T> void ifValuePresent(String path, Consumer<T> ifPresent) {
        get(path).map(ConfigValue::getObject).map(value -> (T) value).ifPresent(ifPresent);
    }

    default boolean isValuePresent(String path) {
        return get(path).isPresent();
    }

    default <T> void ifValuePresent(String path, Class<T> type, Consumer<T> ifPresent) {
        get(path, type).ifPresent(ifPresent);
    }

    default Optional<ConfigValue> get(String path) {
        Map<String, ConfigValue> values;
        if (path.contains("."))
            values = getHierarchyValues();
        else
            values = getValues();

        return Optional.ofNullable(values.get(path));
    }

    default <T> Optional<T> get(String path, Class<T> type) {
        try {
            T as = getAs(path, type);
            return Optional.of(as);
        } catch (Throwable throwable) {
            return Optional.empty();
        }
    }

    default void ensureHasValues(String... values) {
        for (String value : values) {
            Preconditions.checkArgument(get(value).isPresent(), "Failed to find required value " + value + " at " + (this instanceof ConfigSection ? ((ConfigSection) this).getPath() + "." : ""));
        }
    }

    default <T> T getAs(String path) {
        return (T) getAs(path, (Class<? extends Object>) null);
    }

    default void ensureHasAny(String... values) {
        for (String value : values) {
            if (get(value).isPresent()) {
                return;
            }
        }
        throw new IllegalStateException("Failed to find any of the values: " + String.join(", ", values) + " at " + (this instanceof ConfigSection ? ((ConfigSection) this).getPath() + "." : ""));
    }
}
