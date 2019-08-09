package com.oop.orangeEngine.yaml.updater;

import com.oop.orangeEngine.yaml.OConfiguration;
import com.oop.orangeEngine.yaml.value.AConfigurationValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigurationUpdater {

    private OConfiguration updatedConfig;
    private OConfiguration oldConfig;
    private UpdateType method = UpdateType.BLACKLISTED;
    private List<String> listedKeys = new ArrayList<>();
    private List<String> listedValues = new ArrayList<>();

    public ConfigurationUpdater(OConfiguration updatedConfig, OConfiguration oldConfig) {
        this.updatedConfig = updatedConfig;
        this.oldConfig = oldConfig;
    }

    public ConfigurationUpdater method(UpdateType method) {
        this.method = method;
        return this;
    }

    public ConfigurationUpdater listKey(String key) {
        this.listedKeys.add(key);
        return this;
    }

    public ConfigurationUpdater listValue(String value) {
        this.listedValues.add(value);
        return this;
    }

    public int update() {

        AtomicInteger updatedValues = new AtomicInteger();
        List<AConfigurationValue> toUpdate = new ArrayList<>();

        updatedConfig.getAllValues().forEach((k, v) -> {
            if (!oldConfig.getAllValues().containsKey(k)) {

                //First check for which mode is on
                if (method == UpdateType.BLACKLISTED) {

                    if (listedValues.stream().anyMatch(key -> v.key().contains(key))) return;
                    if (listedKeys.stream().anyMatch(key -> v.path().contains(key))) return;

                    toUpdate.add(v);

                } else {

                    if (listedValues.stream().noneMatch(key -> v.key().contains(key))) return;
                    if (listedKeys.stream().noneMatch(key -> v.path().contains(key))) return;

                    toUpdate.add(v);

                }

            }
        });

        toUpdate.forEach(key -> {

            AConfigurationValue value = oldConfig.setValue(key.path(), key);
            if (value != null) {
                updatedValues.incrementAndGet();
                value.description(key.description());
            }

        });

        oldConfig.save();
        return updatedValues.get();

    }

    public enum UpdateType {

        BLACKLISTED,
        WHITELIST

    }

}
