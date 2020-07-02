package com.oop.orangeengine.yaml.interfaces;

import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public interface Sectionable {

    Map<String, ConfigSection> getSections();

    default ConfigSection createSection(String path) {
        return createSection(path, true);
    }

    default ConfigSection createSection(String path, boolean splitAtDots) {
        Optional<ConfigSection> section = getSection(path);
        if (section.isPresent())
            return section.get();

        if (path.contains(".") && splitAtDots) {
            String[] split = path.split("\\.");
            String sectionName = split[split.length - 1];

            ConfigSection currentSection = this instanceof ConfigSection ? (ConfigSection) this : null;
            for (String childName : Arrays.copyOf(split, split.length - 1)) {
                Optional<ConfigSection> child = getSection(childName);
                if (!child.isPresent()) {
                    ConfigSection oldSection = currentSection;

                    if (this instanceof ConfigSection)
                        currentSection = new ConfigSection(childName, (ConfigSection) this);
                    else if (this instanceof Config)
                        currentSection = new ConfigSection(childName, (Config) this);

                    else if (currentSection != null)
                        currentSection = new ConfigSection(childName, currentSection);

                    if (oldSection == null)
                        getSections().put(childName, currentSection);

                    else oldSection.getSections().put(childName, currentSection);
                } else
                    currentSection = child.get();
            }
            return Objects.requireNonNull(currentSection, "Failed to find child at " + path).createSection(sectionName);

        } else {
            ConfigSection newSection = null;
            if (this instanceof ConfigSection)
                newSection = new ConfigSection(path, (ConfigSection) this);
            else if (this instanceof Config)
                newSection = new ConfigSection(path, (Config) this);

            getSections().put(path, newSection);
            return newSection;
        }
    }

    default Optional<ConfigSection> getSection(String path) {
        if (path.contains(".")) {
            String[] split = path.split("\\.");
            String sectionName = split[split.length - 1];

            ConfigSection section = null;
            for (String childName : Arrays.copyOf(split, split.length - 1)) {
                Optional<ConfigSection> child = getSection(childName);
                if (!child.isPresent()) return Optional.empty();

                section = child.get();
            }
            return Objects.requireNonNull(section, "Failed to find child at " + path).getSection(sectionName);

        } else
            return Optional.ofNullable(getSections().get(path));
    }

    default void ifSectionPresent(String path, Consumer<ConfigSection> ifPresent) {
        getSection(path).ifPresent(ifPresent);
    }

    default boolean isSectionPresent(String path) {
        return getSection(path).isPresent();
    }
}
