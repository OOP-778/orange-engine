package com.oop.orangeengine.yaml;

import com.oop.orangeengine.yaml.value.AConfigurationValue;

import java.util.function.Consumer;

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
        return type.cast(getValue(path, ifNotPresent));
    }

    default <T> void ifValuePresent(String path, Class<T> type, Consumer<T> value) {

        T object = type.cast(getValueAsObject(path));
        if (object != null)
            value.accept(object);

    }

    default <T> T getValueAsReq(String path) {
        return (T) getValue(path, null);
    }

    default <T> T getValueAsReq(String path, Class<T> type) {
        return type.cast(getValueAsObject(path));
    }

    default <T> T getValueAsReq(String path, Object ifNotPresent) {
        return (T) getValue(path, ifNotPresent);
    }

    default Object getValue(String path, Object ifNotPresent) {
        Object value = getValueAsObject(path);
        return value != null ? value : ifNotPresent;
    }

}
