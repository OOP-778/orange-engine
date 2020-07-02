package com.oop.orangeengine.yaml;

import com.oop.orangeengine.yaml.interfaces.*;
import com.oop.orangeengine.yaml.util.ConfigUtil;
import com.oop.orangeengine.yaml.util.Writer;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigSection implements Valuable, ConfigHolder, Spaceable, Pathable, Commentable, Writeable {

    @Getter
    public @NonNull String key;
    public @NonNull Config config;
    public Map<String, ConfigValue> values = new LinkedHashMap<>();

    @Getter
    public List<String> comments = new ArrayList<>();

    @Getter
    public Map<String, ConfigSection> sections = new LinkedHashMap<>();

    @Getter
    private ConfigSection parent;

    public ConfigSection(String key, Config config) {
        this.config = config;
        this.key = key;
    }

    public ConfigSection(String key, ConfigSection parent) {
        this.key = key;
        this.parent = parent;
        this.config = parent.getConfig();
    }

    public List<ConfigSection> getParents() {
        List<ConfigSection> parents = new ArrayList<>();
        if (parent == null) return parents;
        parent._getParents(parents);

        return parents;
    }

    private void _getParents(List<ConfigSection> parents) {
        parents.add(this);
        if (parent != null)
            parent._getParents(parents);
    }

    @Override
    public Map<String, ConfigValue> getHierarchyValues() {
        Map<String, ConfigValue> values = new LinkedHashMap<>();
        this.values.values().forEach(value -> values.put(value.getPath(), value));
        sections.values().forEach(section -> section._hierarchyValues(values));
        return values;
    }

    private void _hierarchyValues(Map<String, ConfigValue> values) {
        this.values.values().forEach(value -> values.put(value.getPath(), value));
        sections.values().forEach(section -> section._hierarchyValues(values));
    }

    @Override
    public Map<String, ConfigValue> getValues() {
        return values;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public int getSpaces() {
        return getParents().size() * 2;
    }

    @Override
    public String getPath() {
        return getParents().isEmpty() ? key : getParents().stream().map(ConfigSection::getKey).collect(Collectors.joining(".")) + "." + key;
    }

    @Override
    public void write(Writer writer) {
        String start = ConfigUtil.getSpaces(getSpaces());

        if (!comments.isEmpty()) {
            for (String comment : comments) {
                writer.write(start + "# " + comment);
            }
        }

        writer.write(start + (key.length() == 1 ? "\"" + key + "\"" : key) + ":");

        if (!values.isEmpty()) {
            // Write values
            for (ConfigValue value : values.values()) {
                value.write(writer);
            }
        }

        ConfigSection[] sections = this.sections.values().toArray(new ConfigSection[0]);
        for (int i = 0; i < sections.length; i++) {
            ConfigSection section = sections[i];
            if (i == 1)
                section.write(writer);
            else {
                section.write(writer);
                writer.newLine();
            }
        }
    }
}
