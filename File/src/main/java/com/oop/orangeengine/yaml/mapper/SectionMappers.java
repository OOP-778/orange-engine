package com.oop.orangeengine.yaml.mapper;

import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.mapper.section.loader.ILoader;

import java.util.HashMap;
import java.util.Map;

public class SectionMappers {

    private static Map<Class<?>, ILoader<?>> mappers = new HashMap<>();

    public static <T> T map(ConfigurationSection section, Class<T> to) {

        if (!mappers.containsKey(to)) return null;

        ILoader mapper = mappers.get(to);

        return (T) mapper.load(section);

    }


    public static boolean isMapperPresent(Class clazz) {
        return mappers.containsKey(clazz);
    }

}
