package com.oop.orangeEngine.yaml.mapper.section.saver;

import com.oop.orangeEngine.yaml.OConfiguration;

public abstract class ASaver<T> {

    public abstract void save(String path, OConfiguration configuration, T object);

}
