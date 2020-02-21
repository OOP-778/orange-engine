package com.oop.orangeengine.message.locale;

import com.google.common.collect.Maps;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;

import java.util.List;
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
        section.getValues().forEach((key, value) -> {
            if (value.getValue() instanceof List)
                localeMap.put(key, new OMessage(((List<String>)value.getValue())));

            else
                localeMap.put(key, new OMessage(value.getValueAsReq(String.class)));
        });

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
            YamlMessage.save(message, section.getKey() + "." + id, configuration, description);
            configuration.save();
        }

        return message;
    }
}
