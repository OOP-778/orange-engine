package com.oop.orangeengine.yaml.annotations;

import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.mapper.SectionMappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public interface Configurable {

    default void initConfig(OConfiguration configuration) throws ClassNotFoundException, IllegalAccessException {

        for (Field field : getClass().getDeclaredFields()) {

            field.setAccessible(true);

            for (Annotation annotation : field.getDeclaredAnnotations()) {

                Class reqProductType = null;

                if (annotation instanceof ConfigValue) {

                    ConfigValue configValue = (ConfigValue) annotation;

                    //Check if getValue exists
                    if (!configuration.isPresentValue(configValue.path()))
                        throw new IllegalStateException("Failed to find configuration getValue at (" + configValue.path() + ") at file (" + configuration.getOFile().getFile().getName() + ")");

                    reqProductType = field.getType();
                    Object value = configuration.getValue(configValue.path()).getValue();

                    //Check if configuration getValue is parsable to field type
                    if (value.getClass().isInstance(reqProductType))
                        throw new IllegalStateException("Failed to parse configuration getValue at (" + configValue.path() + ") at file (" + configuration.getOFile().getFile().getName() + ") as " + reqProductType.getTypeName());

                    //Set the field
                    field.set(this, value);

                } else if (annotation instanceof SectionValue) {

                    SectionValue sectionValue = (SectionValue) annotation;

                    //We have to find out if the getSection at given path even exists
                    if (!configuration.isPresentSection(sectionValue.path()))
                        throw new IllegalStateException("Failed to find configuration getSection at (" + sectionValue.path() + ") at file (" + configuration.getOFile().getFile().getName() + ")");

                    //Requires a list of getValues, else it is a single getValue
                    if (field.getGenericType() instanceof ParameterizedType) {

                        //Check if field getValue is initialized
                        if (field.get(this) == null)
                            throw new IllegalStateException("The field list should be initialized in order for configuration to init!");

                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        for (Type t : parameterizedType.getActualTypeArguments()) {

                            reqProductType = Class.forName(t.getTypeName());
                            break;

                        }

                        if (reqProductType == null) continue;

                        //Because requirement class is ready to be used
                        //It's time to look for getSection mapper for required class
                        if (!SectionMappers.isMapperPresent(reqProductType))
                            throw new IllegalStateException("Failed to find Section Mapper for class " + reqProductType.getTypeName());

                        //Great we've got the mapper, now we have to find getSection getSections of the setParent one.
                        //if no getSections are found, or some failed to build, we will provide empty list (null safety)
                        ConfigurationSection section = configuration.getSection(sectionValue.path());

                        List list = (List) field.get(this);

                        for (ConfigurationSection childSection : section.getSections().values()) {

                            Object mappedObject = SectionMappers.map(childSection, reqProductType);
                            list.add(mappedObject);

                        }

                    } else {

                        reqProductType = field.getType();
                        if (reqProductType == null) continue;

                        //Because requirement class is ready to be used
                        //It's time to look for getSection mapper for required class
                        if (!SectionMappers.isMapperPresent(reqProductType))
                            throw new IllegalStateException("Failed to find Section Mapper for class " + reqProductType.getTypeName());

                        //Great we've got the mapper, now we have to find getSection getSections of the setParent one.
                        //if no getSections are found, or some failed to build, we will provide empty list (null safety)
                        ConfigurationSection section = configuration.getSection(sectionValue.path());

                        Object mappedObject = SectionMappers.map(section, reqProductType);

                        //Set the field
                        field.set(this, mappedObject);

                    }

                }

            }

        }

    }

}
