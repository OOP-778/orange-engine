package com.oop.orangeEngine.yaml.value;

import com.oop.orangeEngine.yaml.OConfiguration;
import com.oop.orangeEngine.yaml.mapper.ObjectsMapper;
import com.oop.orangeEngine.yaml.util.ConfigurationUtil;
import com.oop.orangeEngine.yaml.util.CustomWriter;

import java.io.IOException;
import java.util.List;

public class ConfigurationList extends AConfigurationValue {

    private List<Object> values;

    public ConfigurationList(String key, List<Object> values, OConfiguration configuration) {
        super(key, configuration);
        this.values = values;
    }

    public ConfigurationList(String key, List<Object> values) {
        super(key, (OConfiguration) null);
        this.values = values;
    }

    @Override
    public List<Object> getValue() {
        return values;
    }

    @Override
    public void write(CustomWriter bw) throws IOException {

        bw.write(ConfigurationUtil.stringWithSpaces(getSpaces()) + getKey() + ":");
        for (Object value : values) {

            bw.newLine();
            bw.write(ConfigurationUtil.stringWithSpaces(getSpaces()) + "- " + ObjectsMapper.toString(value));

        }

    }
}
