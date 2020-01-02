package com.oop.orangeengine.main.plugin;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.plugin.EnginePlugin;

public interface OComponent<O extends EnginePlugin> {

    default Engine getEngine() {
        return Engine.getEngine();
    }

    default O getPlugin() {
        return (O) getEngine().getOwning();
    }

    boolean load();

    default void disable() {}

    default boolean reload() {
        disable();
        return load();
    }

    default String getName() {
        return getClass().getSimpleName();
    }

}
