package com.oop.orangeEngine.yaml;

import com.oop.orangeEngine.file.OFile;
import com.oop.orangeEngine.yaml.mapper.ObjectsMapper;
import com.oop.orangeEngine.yaml.util.ConfigurationUtil;
import com.oop.orangeEngine.yaml.util.CustomWriter;
import com.oop.orangeEngine.yaml.util.OIterator;
import com.oop.orangeEngine.yaml.util.UnreadString;
import com.oop.orangeEngine.yaml.value.AConfigurationValue;
import com.oop.orangeEngine.yaml.value.ConfigurationList;
import com.oop.orangeEngine.yaml.value.ConfigurationValue;
import org.apache.commons.math3.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static com.oop.orangeEngine.yaml.util.ConfigurationUtil.isValidIndex;
import static java.util.stream.Collectors.toList;

public class OConfiguration implements Valuable {

    private OFile oFile;
    private Map<String, AConfigurationValue> values = new HashMap<>();
    private Map<String, ConfigurationSection> sections = new HashMap<>();
    private List<String> header = new ArrayList<>();

    public OConfiguration(File file) {
        try {

            this.oFile = new OFile(file);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String sCurrentLine;
            int index = 0;
            List<UnreadString> lines = new ArrayList<>();

            boolean ignoring = false;
            while ((sCurrentLine = reader.readLine()) != null) {
                UnreadString unreadString = new UnreadString(index, sCurrentLine);
                if (unreadString.value().contains("#/*")) {
                    ignoring = true;
                    continue;
                } else if (unreadString.value().contains("#*/")) {
                    ignoring = false;
                    continue;
                }
                if (ignoring) continue;

                lines.add(unreadString);
                index++;

            }

            reader.close();
            UnreadString[] array = new UnreadString[lines.size()];
            lines.forEach(line -> array[line.index()] = line);

            ConfigurationSection key = null;

            OIterator<UnreadString> looper = new OIterator<>(array);
            List<UnreadString> mainSections = new ArrayList<>();

            List<String> description = new ArrayList<>();
            while (looper.hasNext()) {

                UnreadString line = looper.next();
                if (line == null) continue;

                if (ConfigurationUtil.firstCharsAfterSpaces(line.value(), 1).equalsIgnoreCase("#")) {

                    //We got a description
                    description.add(line.value());
                    continue;

                }

                if (line.value().contains(":")) {

                    String[] split = line.value().split(":");
                    if (split.length == 1) {

                        UnreadString[] valuesArray = looper.getObjectsCopy(UnreadString.class);
                        int elementIndex = Arrays.asList(valuesArray).indexOf(line);
                        boolean valid = isValidIndex(valuesArray, elementIndex + 1);

                        if (valid && ConfigurationUtil.isList(valuesArray, elementIndex + 1)) {

                            if (ConfigurationUtil.findSpaces(split[0]) >= 2) continue;

                            //Is list
                            List<UnreadString> listValues = looper.nextValuesThatMatches(us -> us.value().contains("-"), true);
                            Pair<String, Integer> parsedKey = ConfigurationUtil.parse(split[0]);
                            ConfigurationList value = new ConfigurationList(parsedKey.getKey(), listValues.stream().map(UnreadString::value).map(string -> ConfigurationUtil.parse(ConfigurationUtil.parse(string).getKey().substring(1)).getKey()).collect(toList()));

                            value.spaces(parsedKey.getValue());
                            value.description(description);
                            value.spaces(0);

                            values.put(value.getKey(), value);

                        } else {
                            if (ConfigurationUtil.findSpaces(split[0]) != 0) continue;
                            mainSections.add(line);
                        }

                    } else {

                        if (ConfigurationUtil.findSpaces(split[0]) >= 2) continue;

                        Pair<String, Integer> parsedKey = ConfigurationUtil.parse(split[0]);
                        ConfigurationValue value = new ConfigurationValue(parsedKey.getKey(), ObjectsMapper.mapObject(ConfigurationUtil.parse(split[1]).getKey()));

                        value.spaces(parsedKey.getValue());
                        value.description(description);
                        value.spaces(0);

                        values.put(value.getKey(), value);

                    }
                }

            }

            for (UnreadString headSection : mainSections) {

                int startingIndex = Arrays.asList(array).indexOf(headSection);
                int endIndex = ConfigurationUtil.findSectionEnd(startingIndex, looper);
                ConfigurationSection section = ConfigurationUtil.loadSection(new OIterator(ConfigurationUtil.copy(array, startingIndex, endIndex)));

                sections.put(section.getKey(), section);

            }

            if (key != null && key.getParent() == null) sections.put(key.getKey(), key);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

    public ConfigurationSection getSection(String path) {

        if (!path.contains("."))
            return getSections().get(path);

        else {

            String[] split = path.split("\\.");
            ConfigurationSection section = null;

            for (int index = 0; index < split.length; index++) {

                String key = split[index];
                if (section == null) section = getSections().get(key);
                else section = section.sections().get(key);

            }

            if (section != null)
                return section;
        }

        return null;

    }

    public <T> T getValueAsReq(String path) {
        return (T) getValue(path);
    }

    public <T> T getValueAsReq(String path, Class<T> type) {
        return type.cast(getValue(path));
    }

    public AConfigurationValue setValue(String path, Object object) {

        AConfigurationValue value = null;
        if (object instanceof AConfigurationValue) {
            value = (AConfigurationValue) object;
        }

        if (!path.contains(".")) {
            if (value == null) value = AConfigurationValue.fromObject(path, object);
            values.put(path, value);
            return value;
        } else {

            String[] split = path.split("\\.");
            ConfigurationSection section = null;
            if (value == null) value = AConfigurationValue.fromObject(split[split.length - 1], object);
            int currentSpaces = 0;

            for (int index = 0; index < split.length - 1; index++) {

                String key = split[index];
                if (section == null) {
                    if (sections.containsKey(key)) {
                        section = sections.get(key);
                    } else {
                        section = new ConfigurationSection(key, currentSpaces);
                        sections.put(key, section);
                    }
                } else {
                    if (section.sections().containsKey(key)) section = section.sections().get(key);
                    else {
                        ConfigurationSection section2 = new ConfigurationSection(key, section.getSpaces());
                        section.assignSection(section2);
                        section = section2;
                    }
                }

                currentSpaces += 2;

            }

            if (section != null) {
                section.getValues().put(split[split.length - 1], value);
                value.spaces(value.spaces() <= 0 ? section.getSpaces() + 2 : value.spaces());
                value.parent(section);
            }
            return value;

        }
    }

    public Map<String, AConfigurationValue> getValues() {
        return values;
    }

    public Map<String, AConfigurationValue> getAllValues() {

        Map<String, AConfigurationValue> allValues = new HashMap<>();

        values.forEach((k, v) -> allValues.put(v.path(), v));
        for (ConfigurationSection section : sections.values()) {
            allValues.putAll(section.getAllValues());
        }

        return allValues;

    }

    public List<ConfigurationSection> getAllSections() {

        List<ConfigurationSection> sections = new ArrayList<>();
        getSections().values().forEach(section -> sections.addAll(section.getAllSections()));

        return sections;

    }

    public Map<String, ConfigurationSection> getSections() {
        return sections;
    }

    public void save() {
        FileWriter w = null;
        CustomWriter bw = null;

        try {

            oFile.createIfNotExists();
            File file = oFile.getFile();
            w = new FileWriter(file);
            bw = new CustomWriter(w);

            bw.newLine();
            bw.writeWithoutSmart("#/*");
            bw.newLine();
            bw.writeWithoutSmart("#Configuration was generated with OrangeEngine!");
            bw.newLine();
            bw.writeWithoutSmart("#An awesome library written by OOP-778");
            bw.newLine();
            bw.newLine();
            bw.writeWithoutSmart("#Support -> https://discord.gg/35fxvm6");
            bw.newLine();
            bw.writeWithoutSmart("#SpigotMC -> https://www.spigotmc.org/members/brian.562713/");
            bw.newLine();
            bw.writeWithoutSmart("#GitLab -> https://gitlab.com/oskardhavel");
            bw.newLine();
            bw.writeWithoutSmart("#---------------------");
            bw.newLine();

            if(!header.isEmpty()) {
                bw.newLine();
                for (String head : header) {
                    bw.writeWithoutSmart("#" + head);
                    bw.newLine();
                }
                bw.newLine();
            }

            bw.writeWithoutSmart("#*/");
            bw.newLine();

            for (AConfigurationValue value : getValues().values()) {

                value.writeDescription(bw, value.spaces());
                value.write(bw);

            }

            for (ConfigurationSection section : sections.values()) {

                section.writeDescription(bw, section.getSpaces());
                section.write(bw);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (w != null) w.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public OFile getOFile() {
        return oFile;
    }

    public void setFile(File file) {
        this.oFile = new OFile(file);
    }

    public boolean isPresentValue(String path) {
        return getValue(path, null) != null;
    }

    public boolean isPresentSection(String child) {
        return getSections().containsKey(child);
    }

    public ConfigurationSection createNewSection(String path) {

        if (!path.contains(".")) {

            ConfigurationSection section = new ConfigurationSection(path, 0);
            sections.put(path, section);
            return section;

        } else {
            String[] split = path.split("\\.");
            ConfigurationSection parent = null;

            for (int index = 0; index < split.length - 1; index++) {

                String key = split[index];
                if (parent == null) parent = getSections().get(key);
                else parent = parent.sections().get(key);

            }

            if (parent != null) {
                String sectionName = split[split.length - 1];
                int spaces = parent.getSpaces() + 2;
                ConfigurationSection section = new ConfigurationSection(sectionName, spaces);
                parent.assignSection(section);
                return section;
            }

        }

        return null;
    }

    public void appendHeader(String string) {
        this.header.add(string);
    }

}
