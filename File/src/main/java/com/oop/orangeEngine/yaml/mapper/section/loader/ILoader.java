package com.oop.orangeEngine.yaml.mapper.section.loader;

import com.oop.orangeEngine.yaml.ConfigurationSection;

import java.lang.reflect.ParameterizedType;

public interface SectionLoader<T> {

    default Class<T> getJavaClass() {
        try {

            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<T>) clazz;

        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic configType!!! Please use extends <> ");
        }
    }

    T map(ConfigurationSection section);

   String getType();

}
