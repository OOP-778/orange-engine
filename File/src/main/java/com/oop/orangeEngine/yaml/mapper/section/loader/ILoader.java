package com.oop.orangeEngine.yaml.mapper.section.loader;

import com.oop.orangeEngine.yaml.ConfigurationSection;

public interface ILoader<T> {

    T load(ConfigurationSection section);

}
