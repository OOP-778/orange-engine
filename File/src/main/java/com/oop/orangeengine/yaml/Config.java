package com.oop.orangeengine.yaml;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.interfaces.Commentable;
import com.oop.orangeengine.yaml.interfaces.Sectionable;
import com.oop.orangeengine.yaml.interfaces.Valuable;
import com.oop.orangeengine.yaml.util.Commentator;
import com.oop.orangeengine.yaml.util.ConfigUtil;
import com.oop.orangeengine.yaml.util.Writer;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringEscapeUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Config implements Valuable, Sectionable, Commentable {
    public static final Yaml yaml = new Yaml();

    @Getter
    public List<String> comments = new ArrayList<>();

    @Getter
    private Map<String, ConfigSection> sections = new LinkedHashMap<>();

    @Getter
    private Map<String, ConfigValue> values = new LinkedHashMap<>();

    @Getter
    private OFile file;

    public Config(@NonNull InputStreamReader reader) {
        ConfigUtil.load(this, yaml, reader);
    }

    public Config(@NonNull OFile file) {
        this.file = file;

        // Load data
        try {
            ConfigUtil.load(this, yaml);
        } catch (Throwable throwable) {
            getEngine().getLogger().error(new IllegalStateException("Failed to load yaml for file: " + file.getFileName(), throwable));
        }
    }

    public Config(@NonNull File file) {
        this(new OFile(file));
    }

    @Override
    public Map<String, ConfigValue> getHierarchyValues() {
        Map<String, ConfigValue> hierarchyValues = new LinkedHashMap<>();
        values.values().forEach(value -> hierarchyValues.put(value.getPath(), value));
        sections.values().forEach(section -> hierarchyValues.putAll(section.getHierarchyValues()));
        return hierarchyValues;
    }

    @Override
    public Map<String, ConfigSection> getHierarchySections() {
        Map<String, ConfigSection> hierarchySections = new LinkedHashMap<>();
        for (ConfigSection section : sections.values())
            hierarchySections.putAll(section.getHierarchySections());

        return hierarchySections;
    }

    @Override
    public Config getConfig() {
        return this;
    }

    @SneakyThrows
    public void save() {
        Writer writer = new Writer(new OutputStreamWriter(new FileOutputStream(file.getFile()), StandardCharsets.UTF_8));

        // Write header
        if (!comments.isEmpty()) {
            writer.write("#<--------------->");
            for (String comment : comments) {
                writer.write("# " + comment);
            }
            writer.write("#<--------------->");
        }

        // Write values
        for (ConfigValue value : values.values()) {
            value.write(writer);
        }

        // Write sections
        for (ConfigSection section : sections.values()) {
            section.write(writer);
            writer.newLine();
        }

        writer.end();
    }
}
