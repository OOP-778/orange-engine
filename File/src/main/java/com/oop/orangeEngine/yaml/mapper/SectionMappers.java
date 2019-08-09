package com.oop.orangeEngine.yaml.mapper;

import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.mapper.section.loader.SectionLoader;

import java.util.HashMap;
import java.util.Map;

public class SectionMappers {

    private static Map<Class, SectionLoader> mappers = new HashMap<>();

    public static <T> T map(ConfigurationSection section, Class<T> to) {

        if (!mappers.containsKey(to)) return null;

        SectionLoader mapper = mappers.get(to);
        if (!mapper.accepts(section)) return null;

        return (T) mapper.map(section);

    }

    public static void register(SectionLoader mapper) {

        if (mappers.containsKey(mapper.productClass)) return;
        mappers.put(mapper.productClass, mapper);

    }

    public static boolean isMapperPresent(Class clazz) {
        return mappers.containsKey(clazz);
    }

}
