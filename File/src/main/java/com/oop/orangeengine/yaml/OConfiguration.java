package com.oop.orangeengine.yaml;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.util.JarUtil;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.mapper.ObjectsMapper;
import com.oop.orangeengine.yaml.mapper.section.ConfigurationSerializable;
import com.oop.orangeengine.yaml.updater.ConfigurationUpdater;
import com.oop.orangeengine.yaml.util.ConfigurationUtil;
import com.oop.orangeengine.yaml.util.CustomWriter;
import com.oop.orangeengine.yaml.util.OIterator;
import com.oop.orangeengine.yaml.util.UnreadString;
import com.oop.orangeengine.yaml.value.AConfigurationValue;
import com.oop.orangeengine.yaml.value.ConfigurationList;
import com.oop.orangeengine.yaml.value.ConfigurationValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.oop.orangeengine.main.Engine.getEngine;
import static com.oop.orangeengine.yaml.util.ConfigurationUtil.isValidIndex;
import static com.oop.orangeengine.yaml.util.ConfigurationUtil.parse;
import static java.util.stream.Collectors.toList;

public class OConfiguration implements Valuable {

    private static Set<Class<? extends ConfigurationSerializable>> defaultSerializers = new HashSet<>();

    private OFile oFile;
    private Map<String, AConfigurationValue> values = new HashMap<>();
    private Map<String, ConfigurationSection> sections = new HashMap<>();
    private List<String> header = new ArrayList<String>() {{
        add("Configuration was generated with OrangeEngine!");
        add("An awesome library written by OOP-778");
        add(" ");
        add("Support -> https://discord.gg/35fxvm6");
        add("SpigotMC -> https://www.spigotmc.org/members/brian.562713/");
        add("GitLab -> https://gitlab.com/oskardhavel");
    }};

    private Set<Class<? extends ConfigurationSerializable>> serializableSet = new HashSet<>();

    public OConfiguration(File file) {
        this(new OFile(file));
    }

    public OConfiguration(OFile oFile) {
        serializableSet.addAll(defaultSerializers);
        try {

            this.oFile = oFile;

            BufferedReader reader = new BufferedReader(new FileReader(oFile.getFile()));
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
            List<OPair<UnreadString, List<String>>> mainSections = new ArrayList<>();

            List<String> description = new ArrayList<>();
            while (looper.hasNext()) {

                UnreadString line = looper.next();
                if (line == null) continue;

                if (line.value().trim().startsWith("#")) {
                    OPair<String, Integer> parsed = parse(line.value().substring(1));
                    if (parsed.getFirst().equalsIgnoreCase("------------------")) continue;

                    description.add(parsed.getFirst());

                } else
                    if (line.value().trim().length() == 0 && !description.isEmpty()) description.add("");

                else if (line.value().contains(":")) {
                    String[] split = ConfigurationUtil.splitAtFirst(line.value(), ':');
                    if (split.length == 1) {

                        UnreadString[] valuesArray = looper.getObjectsCopy(UnreadString.class);
                        int elementIndex = Arrays.asList(valuesArray).indexOf(line);
                        boolean valid = isValidIndex(valuesArray, elementIndex + 1);

                        if (valid && ConfigurationUtil.isList(valuesArray, elementIndex + 1)) {
                            if (ConfigurationUtil.findSpaces(split[0]) >= 2) {
                                description.clear();
                                continue;
                            }

                            //Is list
                            List<UnreadString> listValues = looper.nextValuesThatMatches(us -> us.value().contains("-"), true);

                            OPair<String, Integer> parsedKey = parse(split[0]);
                            ConfigurationList value = new ConfigurationList(parsedKey.getFirst(), listValues.stream().map(UnreadString::value).map(string -> parse(parse(string).getFirst().substring(1)).getFirst()).collect(toList()));

                            value.setSpaces(parsedKey.getSecond());
                            value.description(description);
                            value.setSpaces(0);

                            values.put(value.getKey(), value);
                            description.clear();

                        } else {
                            if (ConfigurationUtil.findSpaces(split[0]) != 0) {
                                description.clear();
                                continue;
                            }
                            mainSections.add(new OPair<>(line, new ArrayList<>(description)));
                            description.clear();
                        }

                    } else {
                        if (ConfigurationUtil.findSpaces(split[0]) >= 2) {
                            description.clear();
                            continue;
                        }
                        OPair<String, Integer> parsedKey = parse(split[0]);

                        AConfigurationValue value;

                        //Check for list
                        if (split[1].trim().startsWith("[]"))
                            value = new ConfigurationList(parsedKey.getFirst(), new ArrayList<>());

                        else
                            value = new ConfigurationValue(parsedKey.getFirst(), ObjectsMapper.mapObject(parse(split[1]).getFirst()));

                        value.setSpaces(parsedKey.getSecond());
                        value.description(description);
                        value.setSpaces(0);

                        values.put(value.getKey(), value);
                        description.clear();
                    }
                }

            }

            for (OPair<UnreadString, List<String>> headSection : mainSections) {
                int startingIndex = Arrays.asList(array).indexOf(headSection.getFirst());
                int endIndex = ConfigurationUtil.findSectionEnd(startingIndex, looper);
                ConfigurationSection section = ConfigurationUtil.loadSection(this, new OIterator(ConfigurationUtil.copy(array, startingIndex, endIndex)));
                section.description(headSection.getSecond());

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
                else section = section.getSections().get(key);

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
                else section = section.getSections().get(key);

            }
            return section;
        }
    }

    public AConfigurationValue setValue(String path, Object object) {
        AConfigurationValue value = null;
        if (object instanceof AConfigurationValue) {
            value = (AConfigurationValue) object;
        }

        if (!path.contains(".")) {
            if (values.containsKey(path)) {
                if (object == null) {
                    values.remove(path);
                    return null;
                }

                AConfigurationValue value2 = values.get(path);
                value2.updateObject(object);

                return value2;
            }
            if (value == null) value = AConfigurationValue.fromObject(path, object);
            values.put(path, value);

            value.setConfiguration(this);
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
                        section = new ConfigurationSection(this, key, currentSpaces);
                        sections.put(key, section);
                    }
                } else {
                    if (section.getSections().containsKey(key))
                        section = section.getSections().get(key);

                    else {
                        ConfigurationSection section2 = new ConfigurationSection(this, key, section.getSpaces());
                        section.assignSection(section2);
                        section = section2;
                    }
                }

                currentSpaces += 2;
            }

            if (section != null)
                section.assignValue(value);

            return value;
        }
    }

    public Map<String, AConfigurationValue> getValues() {
        return values;
    }

    public Map<String, AConfigurationValue> getAllValues() {
        Map<String, AConfigurationValue> allValues = new LinkedHashMap<>();

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

    public void save(OFile file) {
        FileWriter w = null;
        CustomWriter bw = null;

        try {
            oFile.createIfNotExists();
            w = new FileWriter(file.getFile());
            bw = new CustomWriter(w);

            bw.write("#/*");

            if (!header.isEmpty()) {
                for (String head : header) {
                    if (head.trim().length() == 0)
                        bw.newLine();

                    else
                        bw.write("# " + head);
                }
            }

            bw.write("#*/");
            bw.newLine();

            for (AConfigurationValue value : getValues().values()) {
                value.writeDescription(bw, value.getSpaces());
                value.write(bw);
            }

            for (ConfigurationSection section : sections.values()) {
                section.writeDescription(bw, section.getSpaces());
                section.write(bw);
                bw.newLine();
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

    public void save() {
        save(oFile);
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

    public boolean hasValue(String path) {
        return isPresentValue(path);
    }

    public boolean isPresentSection(String sectionName) {
        return getSections().containsKey(sectionName);
    }

    public boolean hasChild(String sectionName) {
        return isPresentSection(sectionName);
    }

    public ConfigurationSection createNewSection(String path) {
        if (!path.contains(".")) {
            if (sections.containsKey(path))
                return sections.get(path);

            ConfigurationSection section = new ConfigurationSection(this, path, 0);
            sections.put(path, section);
            return section;

        } else {
            String[] split = path.split("\\.");
            ConfigurationSection parent = null;

            for (int index = 0; index < split.length - 1; index++) {
                String key = split[index];
                if (parent == null)
                    parent = getSections().get(key);
                else
                    parent = parent.getSections().get(key);
            }

            if (parent != null) {
                String sectionName = split[split.length - 1];
                int spaces = parent.getSpaces() + 2;
                ConfigurationSection section = new ConfigurationSection(this, sectionName, spaces);
                parent.assignSection(section);
                return section;
            }

        }

        return null;
    }

    public void clearDefaultHeader() {
        this.header.clear();
    }

    public void appendHeader(String string) {
        this.header.add(string);
    }

    public void registerSerializer(Class<? extends ConfigurationSerializable> serializable) {
        this.serializableSet.add(serializable);
    }

    public static void registerDefaultSerializer(Class<? extends ConfigurationSerializable> serializable) {
        defaultSerializers.add(serializable);
    }

    Object wrapSection(ConfigurationSection section) {
        assert section != null;
        assert section.isPresentValue("_type_");

        AtomicReference<Object> object = new AtomicReference<>(null);
        String type = section.getValueAsReq("_type_");

        OptionalConsumer.of(serializableSet.stream()
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException ignored) {
                    }
                    return null;
                })
                .filter(serializer -> serializer != null && serializer.getType().equalsIgnoreCase(type))
                .findFirst())
                .ifPresentOrElse(wrapper -> object.set(wrapper.load(section)), () -> Engine.getInstance().getLogger().throwError("Failed to find a section wrapper for type: " + type));

        return object.get();
    }

    public Object wrapSection(String path) {
        ConfigurationSection section = getSection(path);
        return wrapSection(section);
    }

    public <T> T wrapSection(String path, Class<T> type) {
        return type.cast(wrapSection(path));
    }

    private Class getSerializer(Class klass) {
        return serializableSet.stream()
                .filter(serializer -> serializer.isAssignableFrom(klass) || serializer.getSuperclass() == klass)
                .findFirst()
                .orElse(null);
    }

    public ConfigurationUpdater updater() {
        return updater(oFile);
    }

    public ConfigurationUpdater updater(OFile oFile) {
        File folder = oFile.getFolder();
        String fileName = oFile.getFileName();

        JarUtil.copyFileFromJar(fileName, folder, JarUtil.CopyOption.REPLACE_IF_EXIST, "new" + fileName, getEngine().getOwning().getClass());
        OFile newConfigFile = new OFile(folder, "new" + fileName);

        OConfiguration newConfig = new OConfiguration(newConfigFile);
        return new ConfigurationUpdater(newConfig, this);
    }

    public void ifSectionPresent(String path, Consumer<ConfigurationSection> consumer) {
        ConfigurationSection section = getSection(path);
        if (section != null)
            consumer.accept(section);
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
}
