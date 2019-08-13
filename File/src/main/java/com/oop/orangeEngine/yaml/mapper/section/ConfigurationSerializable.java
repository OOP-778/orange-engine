package com.oop.orangeEngine.yaml.mapper.section;

import com.oop.orangeEngine.yaml.mapper.section.loader.ILoader;
import com.oop.orangeEngine.yaml.mapper.section.saver.ISaver;

import java.lang.reflect.ParameterizedType;

public interface ConfigurationSerializable<T> extends ILoader<T>, ISaver<T> {

    String getType();

    default Class<T> getJavaClass() {
        try {

            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<T>) clazz;

        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic configType!!! Please use extends <> ");
        }
    }

}
