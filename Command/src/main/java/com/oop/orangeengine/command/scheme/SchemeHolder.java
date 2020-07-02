package com.oop.orangeengine.command.scheme;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchemeHolder {

    @Getter
    private Map<String, Map<String, Scheme>> schemes = new HashMap<>();

    protected SchemeHolder() {}

    public SchemeHolder(Config config) {
        for (ConfigSection command : config.getSections().values()) {
            Map<String, Scheme> schemeMap = new HashMap<>();
            for (ConfigSection scheme : command.getSections().values())
                schemeMap.put(scheme.getKey().toLowerCase(), Scheme.of(scheme));

            schemes.put(command.getKey().toLowerCase(), schemeMap);
        }
    }

    public Scheme getScheme(OCommand command, String schemeId) {
        Map<String, Scheme> commandMap = Optional.ofNullable(schemes.get(command.getLabel())).orElse(schemes.get("default"));

        Scheme scheme = commandMap.get(schemeId.toLowerCase());
        if (scheme == null)
            throw new IllegalStateException("Failed to find scheme for command: " + command.getLabel() + " id: " + schemeId);

        return scheme;
    }

    public SchemeHolder addDefault(String id, Scheme scheme) {
        Map<String, Scheme> schemeMap = schemes.computeIfAbsent("default", key -> new HashMap<>());
        schemeMap.put(id, scheme);
        return this;
    }
}
