package com.oop.orangeengine.yaml.util;

import com.google.gson.internal.Primitives;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigUtil {

    @SneakyThrows
    public static void load(Config config, Yaml yaml, InputStreamReader reader) {
        String[] lines = new BufferedReader(reader)
                .lines()
                .toArray(String[]::new);
        reader.close();

        Map<Object, Object> data = (Map<Object, Object>) yaml.load(String.join("\n", lines));
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

        Map<String, List<String>> comments = Commentator.comments(lines);
        comments.forEach((path, pathComments) -> {
            if (path.equalsIgnoreCase("#"))
                config.getComments().addAll(pathComments);

            else {
                Optional<ConfigValue> configValue = config.get(path);
                if (!configValue.isPresent()) {
                    config.getSection(path).ifPresent(section -> section.getComments().addAll(pathComments));
                    return;
                }

                configValue.get().getComments().addAll(pathComments);
            }
        });
    }

    @SneakyThrows
    public static void load(Config config, Yaml yaml) {
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(config.getFile().getFile()), StandardCharsets.UTF_8);
        load(config, yaml, inputStreamReader);
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
