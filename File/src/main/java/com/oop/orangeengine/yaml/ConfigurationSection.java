package com.oop.orangeengine.yaml;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.util.ConfigurationUtil;
import com.oop.orangeengine.yaml.util.CustomWriter;
import com.oop.orangeengine.yaml.util.Descriptionable;
import com.oop.orangeengine.yaml.value.AConfigurationValue;
import com.oop.orangeengine.yaml.value.ConfigurationList;
import com.oop.orangeengine.yaml.value.ConfigurationValue;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class ConfigurationSection extends Descriptionable implements Valuable {

    private OConfiguration configuration;
    private String key;
    private ConfigurationSection parent;
    private Map<String, AConfigurationValue> values = new LinkedHashMap<>();
    private Map<String, ConfigurationSection> sections = new LinkedHashMap<>();
    private int spaces;

    public ConfigurationSection(OConfiguration configuration, String key, int spaces) {
        this.configuration = configuration;
        this.key = key;
        this.spaces = spaces;
    }

    private ConfigurationSection setParent(ConfigurationSection parent) {
        parent.sections.put(key, this);
        this.parent = parent;
        return this;
    }

    public Map<String, AConfigurationValue> getValues() {
        return values;
    }

    public Map<String, Object> getValuesConverted() {
        Map<String, Object> values = new HashMap<>();
        getValues().forEach((k, v) -> values.put(k, v.getValue()));
        return values;
    }

    public Map<String, ConfigurationSection> getSections() {
        return sections;
    }

    public void assignSection(ConfigurationSection section) {
        section.setParent(this);
        sections.put(section.getKey(), section);
    }

    public void assignValue(AConfigurationValue value) {
        value.setParent(this);
        value.setSpaces(spaces + 2);
        values.put(value.getKey(), value);
        value.setConfiguration(getConfiguration());
    }

    public List<ConfigurationSection> getAllParents() {

        List<ConfigurationSection> parents = new ArrayList<>();
        getParents(parents);
        return parents;

    }

    public List<ConfigurationSection> getAllSections() {

        List<ConfigurationSection> childs = new ArrayList<>();
        childs.addAll(getSections().values());

        getSections().values().forEach(c -> childs.addAll(c.getAllSections()));

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
                else section = section.getSections().get(key);

            }

            if (section != null && section.getValues().containsKey(valueKey)) return section.getValues().get(valueKey);

        }

        return null;

    }

    void write(CustomWriter bw) throws IOException {

        bw.write(ConfigurationUtil.stringWithSpaces(spaces) + key + ":");

        for (AConfigurationValue value : values.values().stream().filter(o -> o instanceof ConfigurationValue).collect(Collectors.toList())) {

            value.writeDescription(bw, value.getSpaces());
            value.write(bw);

        }


        for (AConfigurationValue value : values.values().stream().filter(o -> o instanceof ConfigurationList).collect(Collectors.toList())) {

            value.writeDescription(bw, value.getSpaces());
            value.write(bw);

        }

        boolean first = true;
        for (ConfigurationSection value : sections.values()) {
            if (first) {
                first = false;
                value.write(bw);

            } else {
                bw.newLine();
                value.write(bw);
            }
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

    public boolean hasValue(String path) {
        return isPresentValue(path);
    }

    public boolean isPresentSection(String sectionName) {
        return getSections().containsKey(sectionName);
    }

    public boolean hasChild(String sectionName) {
        return isPresentSection(sectionName);
    }

    public ConfigurationSection getSection(String sectionName) {
        if (!sectionName.contains(".")) {
            return getSections().get(sectionName);

        } else {
            String[] split = sectionName.split("\\.");
            ConfigurationSection section = null;
            for (int index = 0; index < split.length - 1; index++) {

                String key = split[index];
                if (section == null) section = sections.get(key);
                else section = section.getSections().get(key);

            }

            return section;
        }
    }

    public ConfigurationSection findAcceptableParent(ConfigurationSection newSection) {
        //Check one if createNewSection and this getSection setSpaces are equal
        if (newSection.spaces == spaces) return parent;

        //If this getSection setSpaces are more than createNewSection setSpaces
        if (newSection.spaces < spaces) return parent.findAcceptableParent(newSection);

        return this;
    }

    public AConfigurationValue setValue(String path, Object object) {
        AConfigurationValue value = null;
        if (object instanceof AConfigurationValue)
            value = (AConfigurationValue) object;

        if (!path.contains("\\.")) {
            if (value == null)
                value = AConfigurationValue.fromObject(path, object);

            assignValue(value);

        } else {
            String pathSplit[] = path.split("\\.");
            ConfigurationSection currentSection = this;

            for (String s : pathSplit) {
                ConfigurationSection oldSection = currentSection;
                currentSection = currentSection.getSection(s);
                if (currentSection == null) {
                    currentSection = new ConfigurationSection(getConfiguration(), s, getSpaces() + 2);
                    oldSection.assignSection(currentSection);
                }
            }

            currentSection.setValue(pathSplit[pathSplit.length - 1], object);
        }
        return value;
    }

    @Override
    public <T> T getOrInsert(String path, Class<T> type, T defaultValue) {
        T value = getValueAsReq(path, type);
        if (value == null) {
            setValue(path, defaultValue);
            return defaultValue;
        }

        return value;
    }

    public ConfigurationSection createNewSection(String path) {

        ConfigurationSection configurationSection = new ConfigurationSection(configuration, path, spaces + 2);
        assignSection(configurationSection);

        return configurationSection;

    }

    public Object wrap() {
        return getConfiguration().wrapSection(this);
    }

    public <T> T wrap(Class<T> type) {
        return type.cast(wrap());
    }

    public OFile getFile() {
        return configuration.getOFile();
    }

    public void ifSectionPresent(String path, Consumer<ConfigurationSection> consumer) {
        ConfigurationSection section = getSection(path);
        if (section != null)
            consumer.accept(section);
    }
}
