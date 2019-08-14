package com.oop.orangeengine.yaml.mapper.section;

import com.oop.orangeengine.yaml.mapper.section.loader.ILoader;
import com.oop.orangeengine.yaml.mapper.section.saver.ISaver;

import java.lang.reflect.Type;

public interface ConfigurationSerializable<T> extends ILoader<T>, ISaver<T> {

    String getType();

    String getSectionName(T object);

    default Class<T> getGenericClass() {
        for (Type type : getClass().getGenericInterfaces()) {
            try {
                return  (Class<T>) Class.forName(type.getTypeName().substring(2, type.getTypeName().length() - 1));
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return null;
    }

}
