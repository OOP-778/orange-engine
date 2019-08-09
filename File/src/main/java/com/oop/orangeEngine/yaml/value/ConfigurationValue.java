package com.oop.orangeEngine.yaml.value;

import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.mapper.ObjectsMapper;
import com.oop.orangeEngine.yaml.util.ConfigurationUtil;
import com.oop.orangeEngine.yaml.util.CustomWriter;

import java.io.IOException;

public class ConfigurationValue extends AConfigurationValue {

    private Object value;

    public ConfigurationValue(String key, Object value) {
        super(key);
        this.value = value;
    }

    public ConfigurationValue(String key, Object value, ConfigurationSection parent) {
        super(key, parent);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void write(CustomWriter bw) throws IOException {
        bw.write(ConfigurationUtil.stringWithSpaces(spaces()) + key() + ": " + ObjectsMapper.toString(getValue()));
    }
}
