package com.oop.orangeengine.yaml.mapper.section.loader;

import com.oop.orangeengine.yaml.ConfigurationSection;

public interface ILoader<T> {

    T load(ConfigurationSection section);

}
