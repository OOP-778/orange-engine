package com.oop.orangeengine.main.plugin;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class PluginComponentController {
    private final LinkedHashSet<OComponent> reloadableComponents = Sets.newLinkedHashSet();
    private final LinkedHashSet<OComponent> components = Sets.newLinkedHashSet();
    private final Map<Class<? extends OComponent>, List<Consumer<OComponent>>> reloadListeners = new HashMap<>();

    public PluginComponentController add(@NonNull OComponent component, boolean reloadable) {
        components.add(component);
        if (reloadable)
            reloadableComponents.add(component);

        return this;
    }

    public <T extends OComponent> PluginComponentController listenForReload(@NonNull Class<T> clazz, Consumer<T> consumer) {
        reloadListeners.computeIfAbsent(clazz, k -> new ArrayList<>())
                .add((Consumer<OComponent>) consumer);
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

            Optional.ofNullable(
                    reloadListeners
                            .get(component.getClass())
            ).ifPresent(listeners -> listeners.forEach(consumer -> consumer.accept(component)));
        }

        return true;
    }

}
