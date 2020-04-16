package com.oop.orangeengine.yaml;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.mapper.PrimitveMapper;
import com.oop.orangeengine.yaml.value.AConfigurationValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Valuable {

    AConfigurationValue getValue(String path);

    default Object getValueAsObject(String path) {
        AConfigurationValue value = getValue(path);
        if(value == null)
            return null;

        else
            return value.getValue();
    }

    default <T> T getValue(String path, Class<T> type, T ifNotPresent) {
        Object value = getValue(path, ifNotPresent);
        if (value.getClass().isAssignableFrom(type))
            return (T) value;

        else
            return (T)PrimitveMapper.remap(value, type);
    }

    default <T> void ifValuePresent(String path, Class<T> type, Consumer<T> handler) {
        Object value = getValueAsObject(path);
        if(value != null) {
            if (value.getClass().isAssignableFrom(type))
                handler.accept((T) value);

            else
                handler.accept((T) PrimitveMapper.remap(value, type));
        }
    }

    default <T> T getValueAsReq(String path) {
        return (T) getValue(path, null);
    }

    default <T> T getValueAsReq(String path, Class<T> type) {
        Object value = getValueAsObject(path);
        if (value == null) return null;

        if (type == String.class)
            return (T) String.valueOf(value);

        if (value.getClass().isAssignableFrom(type))
            return type.cast(value);

        else
            return (T) PrimitveMapper.remap(value, type);
    }

    default <T> T getValueAsReq(String path, Object ifNotPresent) {
        return (T) getValue(path, ifNotPresent);
    }

    default Object getValue(String path, Object ifNotPresent) {
        Object value = getValueAsObject(path);
        return value != null ? value : ifNotPresent;
    }

    AConfigurationValue setValue(String path, Object object);

    <T> T getOrInsert(String path, Class<T> type, T defaultValue);
}
