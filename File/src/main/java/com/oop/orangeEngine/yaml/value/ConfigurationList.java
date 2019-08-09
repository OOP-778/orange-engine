package com.oop.orangeEngine.yaml.value;

import com.oop.orangeEngine.yaml.mapper.ObjectsMapper;
import com.oop.orangeEngine.yaml.util.ConfigurationUtil;
import com.oop.orangeEngine.yaml.util.CustomWriter;

import java.io.IOException;
import java.util.List;

public class ConfigurationList extends AConfigurationValue {

    private List<Object> values;

    public ConfigurationList(String key, List<Object> values) {
        super(key, null);
        this.values = values;
    }

    @Override
    public Object getValue() {
        return values;
    }


    @Override
    public void write(CustomWriter bw) throws IOException {

        bw.write(ConfigurationUtil.stringWithSpaces(spaces()) + key() + ":");
        for (Object value : values) {

            bw.newLine();
            bw.write(ConfigurationUtil.stringWithSpaces(spaces()) + "- " + ObjectsMapper.toString(value));

        }

    }
}
