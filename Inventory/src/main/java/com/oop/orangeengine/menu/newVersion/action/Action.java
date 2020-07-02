package com.oop.orangeengine.menu.newVersion.action;

import com.oop.orangeengine.menu.newVersion.targets.MenuSlotTarget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Action<T> {

    private Map<Class<? extends MenuSlotTarget>, Set<Predicate<? extends MenuSlotTarget>>> filters = new HashMap<>();
    private Set<Class<? extends MenuSlotTarget>> acceptsTargets = new HashSet<>();
    private Map<Class<? extends MenuSlotTarget>, Set<Consumer<? extends MenuSlotTarget>>> handlers = new HashMap<>();

    public <E extends MenuSlotTarget> T addFilter(Class<E> event, Predicate<E> filter) {
        Set<Predicate<? extends MenuSlotTarget>> events = filters.computeIfAbsent(event, key -> new HashSet<>());
        events.add(filter);
        return returnThis();
    }

    public <E extends MenuSlotTarget> T handle(Class<E> event, Consumer<E> handler) {
        Set<Consumer<? extends MenuSlotTarget>> consumers = handlers.computeIfAbsent(event, key -> new HashSet<>());
        consumers.add(handler);
        return returnThis();
    }

    public T clearFilters() {
        filters.clear();
        return returnThis();
    }

    public T clearTargets() {
        acceptsTargets.clear();
        return returnThis();
    }

    public <E extends MenuSlotTarget> T acceptTarget(Class<E> event) {
        acceptsTargets.add(event);
        return returnThis();
    }

    protected abstract T returnThis();
}
