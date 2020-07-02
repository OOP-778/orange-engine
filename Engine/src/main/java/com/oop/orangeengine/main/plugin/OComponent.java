package com.oop.orangeengine.main.plugin;

import com.oop.orangeengine.main.Engine;

public interface OComponent<O extends EnginePlugin> {

    default Engine getEngine() {
        return Engine.getEngine();
    }

    default O getPlugin() {
        return (O) getEngine().getOwning();
    }

    default boolean load() {
        return true;
    }

    default void disable() {}

    default boolean reload() {
        disable();
        return load();
    }

    default String getName() {
        return getClass().getSimpleName();
    }

}
