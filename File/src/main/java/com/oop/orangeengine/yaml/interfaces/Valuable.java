package com.oop.orangeengine.yaml.interfaces;

import com.google.common.base.Preconditions;
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
        if (path.contains(".")) {
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
        return getAs(path, (Supplier<T>) null);
    }

    default <T> T getAs(String path, Class<T> type, Supplier<T> supplier) {
        return getAs(path, supplier);
    }

    default <T> void ifValuePresent(String path, Consumer<T> ifPresent) {
        get(path).map(ConfigValue::getObject).map(value -> (T) value).ifPresent(ifPresent);
    }

    default boolean isValuePresent(String path) {
        return get(path).isPresent();
    }

    default <T> void ifValuePresent(String path, Class<T> type, Consumer<T> ifPresent) {
        get(path).map(ConfigValue::getObject).map(value -> (T) value).ifPresent(ifPresent);
    }

    default Optional<ConfigValue> get(String path) {
        Map<String, ConfigValue> values;
        if (path.contains("."))
            values = getHierarchyValues();
        else
            values = getValues();

        return Optional.ofNullable(values.get(path));
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
