package com.oop.orangeengine.yaml.util;

import com.google.gson.internal.Primitives;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigUtil {

    @SneakyThrows
    public static void load(Config config, Yaml yaml) {
        Map<Object, Object> data = (Map<Object, Object>) yaml.load(new InputStreamReader(new FileInputStream(config.getFile().getFile()), "UTF-8"));
        if (data == null) return;
        data.forEach((key, value) -> {
            if (value instanceof Map) {
                ConfigSection section = config.createSection(key.toString());
                initializeSection(section, (Map<Object, Object>) value);
                config.getSections().put(key.toString(), section);

            } else {
                ConfigValue configValue = new ConfigValue(key.toString(), config);
                configValue.setObject(value);
                config.getValues().put(key.toString(), configValue);
            }
        });
    }

    public static void initializeSection(@NonNull ConfigSection section, Map<Object, Object> map) {
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                ConfigSection child = section.createSection(key.toString());
                initializeSection(child, (Map<Object, Object>) value);
                section.getSections().put(key.toString(), child);

            } else {
                ConfigValue configValue = new ConfigValue(key.toString(), section);
                configValue.setObject(value);
                section.getValues().put(key.toString(), configValue);
            }
        });
    }

    public static String getSpaces(int amount) {
        return IntStream.range(1, amount).mapToObj(i -> " ").collect(Collectors.joining());
    }

    public static boolean isPrimitive(Object object) {
        Class primitive = Primitives.wrap(object.getClass());
        return primitive == Integer.class || (primitive == Float.class || (primitive == Long.class || (primitive == Double.class || (primitive == Boolean.class))));
    }
}
