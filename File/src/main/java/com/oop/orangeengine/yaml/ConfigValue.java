package com.oop.orangeengine.yaml;

import com.oop.orangeengine.yaml.interfaces.*;
import com.oop.orangeengine.yaml.util.ConfigUtil;
import com.oop.orangeengine.yaml.util.Writer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigValue implements ConfigHolder, Spaceable, Pathable, Writeable {

    @Getter
    public List<String> comments = new ArrayList<>();
    @Getter
    private String key;
    private Config config;
    private ConfigSection section;
    @Getter
    @Setter
    private Object object;

    public ConfigValue(String key, @NonNull Config config) {
        this.config = config;
        this.key = key;
    }

    public ConfigValue(String key, @NonNull ConfigSection section) {
        this.section = section;
        this.key = key;
    }

    @Override
    public Config getConfig() {
        return section == null ? config : section.getConfig();
    }

    @Override
    public int getSpaces() {
        return section == null ? 0 : section.getSpaces() + 2;
    }

    @Override
    public String getPath() {
        return (section == null ? "" : (section.getPath() + ".")) + key;
    }

    @Override
    public void write(Writer writer) {
        String start = ConfigUtil.getSpaces(getSpaces());
        if (object == null) return;

        if (!comments.isEmpty()) {
            writer.newLine();
            for (String comment : comments)
                writer.write(start + "# " + comment);
        }

        if (object instanceof Collection) {
            if (((Collection) object).isEmpty()) {
                writer.write(start + key + ": []");

            } else {
                writer.write(start + key + ":");
                for (Object listObject : (Collection<Object>) object) {
                    if (ConfigUtil.isPrimitive(listObject))
                        writer.write(start + "- " + listObject);
                    else
                        writer.write(start + "- \"" + dumpObject(listObject) + "\"");
                }
            }
        } else if (ConfigUtil.isPrimitive(object))
            writer.write(start + key + ": " + object);
        else
            writer.write(start + key + ": \"" + dumpObject(object) + "\"");
    }

    public String dumpObject(Object value) {
        return StringEscapeUtils.escapeJava(value.toString());
    }

    public boolean isList() {
        return getObject() instanceof List;
    }

    public <T> List<T> getAsList(Class<T> type) {
        return (List<T>) object;
    }

    public <T> T getAs(Class<T> clazz) {
        return (T) Valuable.doConversion(object, clazz);
    }
}
