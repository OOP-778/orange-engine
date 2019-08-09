package com.oop.orangeEngine.yaml.value;

import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.util.CustomWriter;
import com.oop.orangeEngine.yaml.util.Descriptionable;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AConfigurationValue extends Descriptionable {

    private String key;
    private ConfigurationSection parent;
    private int spaces = 0;

    public AConfigurationValue(String key, ConfigurationSection parent) {
        this.key = key;
        this.parent = parent;
    }

    public AConfigurationValue(String key) {
        this(key, null);
    }

    public static AConfigurationValue fromObject(String key, Object obj) {

        AConfigurationValue value;

        if (obj instanceof ArrayList)
            value = new ConfigurationList(key, (List<Object>) obj);
        else
            value = new ConfigurationValue(key, obj);

        return value;

    }

    public int spaces() {
        return spaces;
    }

    public AConfigurationValue spaces(int spaces) {
        this.spaces = spaces;
        return this;
    }

    public ConfigurationSection parent() {
        return parent;
    }

    public AConfigurationValue parent(ConfigurationSection section) {
        this.parent = section;
        return this;
    }

    public String path() {
        return parent == null ? key() : parent.getKey() + "." + key();
    }

    public String key() {
        return key;
    }

    public abstract Object getValue();

    public <T> T getValueAsReq() {
        return (T) getValue();
    }

    public <T> T getValueAsReq(Class<T> type) {
        return type.cast(getValue());
    }

    public abstract void write(CustomWriter bw) throws IOException;

}
