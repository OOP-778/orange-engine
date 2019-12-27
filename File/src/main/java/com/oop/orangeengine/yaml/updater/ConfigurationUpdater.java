package com.oop.orangeengine.yaml.updater;

import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.value.AConfigurationValue;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ConfigurationUpdater {

    private OConfiguration updatedConfig;
    private OConfiguration oldConfig;
    private UpdateType method = UpdateType.BLACKLISTED;
    private List<Predicate<String>> listedPaths = new ArrayList<>();

    public ConfigurationUpdater(OConfiguration updatedConfig, OConfiguration oldConfig) {
        this.updatedConfig = updatedConfig;
        this.oldConfig = oldConfig;
    }

    public ConfigurationUpdater method(UpdateType method) {
        this.method = method;
        return this;
    }

    public ConfigurationUpdater addPathFilter(Predicate<String> pathFilter) {
        this.listedPaths.add(pathFilter);
        return this;
    }

    public ConfigurationUpdater listPath(String path) {
        this.listedPaths.add(path2 -> path2.contains(path));
        return this;
    }

    public int update() {
        AtomicInteger updatedValues = new AtomicInteger();
        List<AConfigurationValue> toUpdate = new ArrayList<>();

        updatedConfig.getAllValues().forEach((k, v) -> {
            if (!oldConfig.getAllValues().containsKey(k)) {

                // First check for which mode is on
                if (method == UpdateType.BLACKLISTED) {
                    if (listedPaths.stream().anyMatch(filter -> filter.test(v.path()))) return;
                    toUpdate.add(v);

                } else if (method == UpdateType.WHITELIST) {
                    if (listedPaths.stream().noneMatch(filter -> filter.test(v.path()))) return;
                    toUpdate.add(v);

                } else
                    toUpdate.add(v);
            }
        });

        toUpdate.forEach(key -> {

            AConfigurationValue value = oldConfig.setValue(key.path(), key);
            if (value != null) {
                updatedValues.incrementAndGet();
                value.description(key.description());
            }

        });

        if (updatedValues.get() > 0)
            oldConfig.save();

        deleteOld();
        return updatedValues.get();

    }

    public enum UpdateType {

        BLACKLISTED,
        WHITELIST,
        NONE

    }

    public void deleteOld() {
        updatedConfig.getOFile().getFile().setLastModified(System.currentTimeMillis());
        updatedConfig.getOFile().delete();
    }

}
