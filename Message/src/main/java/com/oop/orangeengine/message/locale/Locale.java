package com.oop.orangeengine.message.locale;

import com.google.common.collect.Maps;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Locale {

    private static Locale localeInstance;
    private Config configuration;
    private ConfigSection section;

    private Map<String, OMessage> localeMap = Maps.newHashMap();

    private Locale(String locale) {
        OFile file = new OFile(getEngine().getOwning().getDataFolder(), "locale.yml").createIfNotExists();
        configuration = new Config(file);
        section = configuration.createSection(locale);

        // Load from values
        section.getValues().forEach((key, value) -> localeMap.put(key, YamlMessage.Chat.load(value)));

        // Load from sections
        section.getSections().forEach((key, localeSection) -> localeMap.put(localeSection.getKey(), YamlMessage.load(localeSection)));
    }

    public static void load(String locale) {
        localeInstance = new Locale(locale);
    }

    public static Locale getLocale() {
        return localeInstance;
    }

    public OMessage getMessage(String id, Supplier<OMessage> ifNotFound, boolean flattened, String... description) {
        id = id.toLowerCase().replace("_", flattened ? " " : ".");
        OMessage message = localeMap.get(id);

        if (message == null) {
            message = ifNotFound.get();
            YamlMessage.save(message, section.getKey() + "." + id, configuration);
            configuration.save();
        }

        return message;
    }
}
