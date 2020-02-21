package com.oop.orangeengine.yaml.value;

import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.util.CustomWriter;
import com.oop.orangeengine.yaml.util.Descriptionable;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AConfigurationValue extends Descriptionable {

    @Setter
    private String key;
    private ConfigurationSection parent;
    @Setter
    private OConfiguration configuration;
    @Setter
    private int spaces = 0;

    public AConfigurationValue(String key, ConfigurationSection parent) {
        this.key = key;
        this.parent = parent;
        if(parent != null)
            this.configuration = parent.getConfiguration();
    }

    public AConfigurationValue(String key, OConfiguration configuration) {
        this.key = key;
        this.configuration = configuration;
        this.parent = null;
    }

    public static AConfigurationValue fromObject(String key, Object obj) {
        AConfigurationValue value;

        if (obj instanceof ArrayList)
            value = new ConfigurationList(key, (List<Object>) obj);
        else
            value = new ConfigurationValue(key, obj);

        return value;

    }

    public AConfigurationValue setParent(ConfigurationSection section) {
        this.parent = section;
        return this;
    }

    public String path() {
        return parent == null ? getKey() : parent.getKey() + "." + getKey();
    }

    public abstract Object getValue();

    public abstract void updateObject(Object object);

    public <T> T getValueAsReq() {
        return (T) getValue();
    }

    public <T> T getValueAsReq(Class<T> type) {
        return type.cast(getValue());
    }

    public abstract void write(CustomWriter bw) throws IOException;

}
