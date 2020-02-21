package com.oop.orangeengine.yaml.value;

import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.mapper.ObjectsMapper;
import com.oop.orangeengine.yaml.util.ConfigurationUtil;
import com.oop.orangeengine.yaml.util.CustomWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ConfigurationList extends AConfigurationValue {
  private List<Object> values;

  public ConfigurationList(String key, List<Object> values, OConfiguration configuration) {
    super(key, configuration);
    this.values = values;
  }

  public ConfigurationList(String key, List<Object> values) {
    super(key, (OConfiguration)null);
    this.values = values;
  }

  public List<Object> getValue() {
    return this.values;
  }

  public void write(CustomWriter bw) throws IOException {
    if (this.values.isEmpty()) {
      bw.write(ConfigurationUtil.stringWithSpaces(this.getSpaces()) + this.getKey() + ": []");
    } else {
      bw.write(ConfigurationUtil.stringWithSpaces(this.getSpaces()) + this.getKey() + ":");
    }

    Iterator var2 = this.values.iterator();

    while(var2.hasNext()) {
      Object value = var2.next();
      bw.write(ConfigurationUtil.stringWithSpaces(this.getSpaces()) + "- " + ObjectsMapper.toString(value));
    }

  }

  public void updateObject(Object object) {
    if (object instanceof List) {
      this.values = (List)object;
    }
  }
}