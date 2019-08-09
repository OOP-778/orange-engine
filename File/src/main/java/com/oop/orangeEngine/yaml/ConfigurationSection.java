package com.oop.orangeEngine.yaml;

import com.oop.orangeEngine.yaml.util.ConfigurationUtil;
import com.oop.orangeEngine.yaml.util.CustomWriter;
import com.oop.orangeEngine.yaml.util.Descriptionable;
import com.oop.orangeEngine.yaml.value.AConfigurationValue;
import com.oop.orangeEngine.yaml.value.ConfigurationList;
import com.oop.orangeEngine.yaml.value.ConfigurationValue;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ConfigurationSection extends Descriptionable implements Valuable {

    private String key;
    private ConfigurationSection parent;
    private Map<String, AConfigurationValue> values = new HashMap<>();
    private Map<String, ConfigurationSection> sections = new HashMap<>();
    private int spaces;

    public ConfigurationSection(String key, int spaces) {
        this.key = key;
        this.spaces = spaces;
    }

    ConfigurationSection parent(ConfigurationSection parent) {
        parent.sections.put(key, this);
        this.parent = parent;
        return this;
    }

    Map<String, AConfigurationValue> getValues() {
        return values;
    }

    public Map<String, Object> getValuesConverted() {

        Map<String, Object> values = new HashMap<>();
        getValues().forEach((k, v) -> values.put(k, v.getValue()));
        return values;

    }

    public Map<String, ConfigurationSection> sections() {
        return sections;
    }

    public void assignSection(ConfigurationSection section) {
        section.parent(this);
        sections.put(section.getKey(), section);
    }

    public void assignValue(AConfigurationValue value) {
        value.parent(this);
        value.spaces(spaces + 2);
        values.put(value.key(), value);
    }

    public List<ConfigurationSection> getAllParents() {

        List<ConfigurationSection> parents = new ArrayList<>();
        getParents(parents);
        return parents;

    }

    public List<ConfigurationSection> getAllSections() {

        List<ConfigurationSection> childs = new ArrayList<>();
        childs.addAll(sections().values());

        sections().values().forEach(c -> childs.addAll(c.getAllSections()));

        return childs;

    }

    private void getParents(List<ConfigurationSection> parents) {

        parents.add(this);
        if (parent != null) parent.getParents(parents);

    }

    public ConfigurationSection getMainParent() {
        if (parent == null) return this;
        else return parent.getMainParent();
    }

    @Override
    public AConfigurationValue getValue(String path) {

        if (!path.contains(".")) {

            return values.getOrDefault(path, null);

        } else {

            String[] split = path.split("\\.");
            String valueKey = split[split.length - 1];

            ConfigurationSection section = null;

            for (int index = 0; index < split.length - 1; index++) {

                String key = split[index];
                if (section == null) section = sections.get(key);
                else section = section.sections().get(key);

            }

            if (section != null && section.getValues().containsKey(valueKey)) return section.getValues().get(valueKey);

        }

        return null;

    }


    void write(CustomWriter bw) throws IOException {

        bw.write(ConfigurationUtil.stringWithSpaces(spaces) + key + ":");

        for (AConfigurationValue value : values.values().stream().filter(o -> o instanceof ConfigurationValue).collect(Collectors.toList())) {

            value.writeDescription(bw, value.spaces());
            value.write(bw);

        }


        for (AConfigurationValue value : values.values().stream().filter(o -> o instanceof ConfigurationList).collect(Collectors.toList())) {

            value.writeDescription(bw, value.spaces());
            value.write(bw);

        }

        for (ConfigurationSection value : sections.values()) {
            value.write(bw);
        }

    }

    public String getPath() {

        List<ConfigurationSection> parents = getAllParents();
        Collections.reverse(parents);

        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (ConfigurationSection parent : parents) {

            if (count != (parents.size() - 1))
                builder.append(parent.getKey()).append(".");
            else
                builder.append(parent.getKey());

            count++;
        }

        return builder.toString();

    }

    public Map<String, AConfigurationValue> getAllValues() {

        Map<String, AConfigurationValue> allValues = new HashMap<>();
        getAllValues(allValues);

        return allValues;
    }

    private void getAllValues(Map<String, AConfigurationValue> allValues) {

        values.forEach((k, v) -> allValues.put(v.path(), v));
        sections.values().forEach(c -> c.getAllValues(allValues));

    }

    public boolean isPresentValue(String path) {
        return getValue(path, null) != null;
    }

    public boolean isPresentSection(String child) {
        return sections().containsKey(child);
    }

    public ConfigurationSection getSection(String sectionName) {

        if (!sectionName.contains(".")) {

            return sections().get(sectionName);

        } else {

            String[] split = sectionName.split("\\.");

            ConfigurationSection section = null;

            for (int index = 0; index < split.length - 1; index++) {

                String key = split[index];
                if (section == null) section = sections.get(key);
                else section = section.sections().get(key);

            }

            return section;
        }

    }

    public ConfigurationSection findAcceptableParent(ConfigurationSection newSection) {

        //Check one if createNewSection and this getSection spaces are equal
        if (newSection.spaces == spaces) return parent;

        //If this getSection spaces are more than createNewSection spaces
        if (newSection.spaces < spaces) return parent.findAcceptableParent(newSection);

        return this;

    }

    public AConfigurationValue setValue(String path, Object object) {

        AConfigurationValue value = null;
        if (object instanceof AConfigurationValue) {
            value = (AConfigurationValue) object;
        }

        if (value == null) value = AConfigurationValue.fromObject(path, object);
        assignValue(value);
        return value;

    }

    public ConfigurationSection createNewSection(String path) {

        ConfigurationSection configurationSection = new ConfigurationSection(path, spaces + 2);
        assignSection(configurationSection);

        return configurationSection;

    }

}
