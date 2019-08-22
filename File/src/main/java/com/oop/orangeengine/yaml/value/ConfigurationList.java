package com.oop.orangeengine.yaml.value;

import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.mapper.ObjectsMapper;
import com.oop.orangeengine.yaml.util.ConfigurationUtil;
import com.oop.orangeengine.yaml.util.CustomWriter;

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

        if(values.isEmpty())
            bw.write(ConfigurationUtil.stringWithSpaces(getSpaces()) + getKey() + ": []");

        else
            bw.write(ConfigurationUtil.stringWithSpaces(getSpaces()) + getKey() + ":");

        for (Object value : values) {
            bw.write(ConfigurationUtil.stringWithSpaces(getSpaces()) + "- " + ObjectsMapper.toString(value));
        }

    }

    @Override
    public void updateObject(Object object) {
        if(object instanceof List)
            values = (List<Object>) object;
    }
}
