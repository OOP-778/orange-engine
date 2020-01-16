package com.oop.orangeengine.main.plugin;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;

import java.util.LinkedHashSet;

@Getter
public class PluginComponentController {

    private LinkedHashSet<OComponent> reloadableComponents = Sets.newLinkedHashSet();
    private LinkedHashSet<OComponent> components = Sets.newLinkedHashSet();

    public PluginComponentController add(@NonNull OComponent component, boolean reloadable) {
        components.add(component);
        if (reloadable)
            reloadableComponents.add(component);

        return this;
    }

    public boolean load() {
        for (OComponent component : components) {
            if (!component.load())
                return false;
        }

        return true;
    }

    public boolean reload() {
        for (OComponent component : reloadableComponents) {
            if (!component.reload())
                return false;
        }

        return true;
    }

}
