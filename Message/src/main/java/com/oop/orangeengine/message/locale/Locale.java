package com.oop.orangeengine.message.locale;

import com.google.common.collect.Maps;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;

import java.util.Map;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Locale {

    private static Locale localeInstance;
    private OConfiguration configuration;
    private ConfigurationSection section;

    private Map<String, OMessage> localeMap = Maps.newHashMap();

    private Locale(String locale) {
        OFile file = new OFile(getEngine().getOwning().getDataFolder(), "locale.yml").createIfNotExists();
        configuration = new OConfiguration(file);
        section = configuration.createNewSection(locale);

        // Load from values
        section.getValues().forEach((key, value) -> localeMap.put(key, YamlMessage.fromValue(value.getValueAsReq())));

        // Load from sections
        section.getSections().forEach((key, localeSection) -> localeMap.put(localeSection.getKey(), YamlMessage.fromSection(localeSection)));
    }

    public static void load(String locale) {
        localeInstance = new Locale(locale);
    }

    public static Locale getLocale() {
        return localeInstance;
    }

    public OMessage getMessage(String id, Supplier<OMessage> ifNotFound) {
        id = id.toLowerCase().replace("_", " ");
        OMessage message = localeMap.get(id);
        if (message == null) {
            message = ifNotFound.get();
            YamlMessage.saveToConfig(message, configuration, section.getKey() + "." + id);
            configuration.save();
        }

        return message;
    }
}
