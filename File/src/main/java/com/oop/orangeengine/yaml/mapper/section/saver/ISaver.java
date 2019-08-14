package com.oop.orangeengine.yaml.mapper.section.saver;

import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;

public interface ISaver<T> {

    default void save(String path, OConfiguration configuration, T object) {
        ConfigurationSection section = configuration.createNewSection(path);
        save(section, object);
    }

    void save(ConfigurationSection section, T object);

}
