package com.oop.orangeengine.yaml;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.NonNull;

import java.util.Optional;

public abstract class WrappedSection {

    private ConfigSection section;
    private int updated = 0;

    public WrappedSection(@NonNull Config config, @NonNull String section) {
        this.section = config.createSection(section);
    }

    public WrappedSection(@NonNull ConfigSection section) {
        this.section = section;
    }

    public WrappedSection(@NonNull ConfigSection section, String sectionName) {
        this.section = section.createSection(sectionName);
    }

    public void defaults(OPair<String, Object>... pairs) {
        for (OPair<String, Object> pair : pairs) {
            Optional<ConfigValue> configValue = section.get(pair.getFirst());
            if (!configValue.isPresent()) {
                section.set(pair.getFirst(), pair.getSecond());
                updated++;
            }
        }
    }

    public boolean shouldSave() {
        return updated > 0;
    }

    public void save() {
        section.getConfig().save();
    }
}
