package com.oop.orangeengine.main.plugin;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.task.TaskController;
import com.oop.orangeengine.main.util.DisablePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Comparator;

public interface EngineBootstrap {

    /**
     * JavaPlugin that started this
     */
    JavaPlugin getStarter();

    default String getName() {
        return getStarter().getName();
    }

    default boolean isDisabling() {
        return getEngine().isDisabling();
    }

    default void setDisabling(boolean disabling) {
        getEngine().setDisabling(disabling);
    }

    default Engine getEngine() {
        return Engine.getEngine();
    }

    default OLogger getOLogger() {
        return getEngine().getLogger();
    }

    default void onEnable() {
        new Engine(this);

        // Init data folder
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        // Enable plugin
        enable();
    }

    default void onDisable() {
        // Disable tasks
        getEngine().getOnDisableRun().entrySet().stream()
                .sorted(Comparator.comparing(t -> t.getKey().getOrder()))
                .forEach(ds -> ds.getValue().run());

        disable();

    }

    default void onDisable(Runnable runnable) {
        getEngine().getOnDisableRun().put(DisablePriority.MIDDLE, runnable);
    }

    default PluginComponentController getPluginComponentController() {
        return getEngine().getPluginComponentController();
    }

    default void onDisable(Runnable runnable, DisablePriority priority) {
        getEngine().getOnDisableRun().put(priority, runnable);
    }

    default TaskController getTaskController() {
        return getEngine().getTaskController();
    }

    void enable();

    default void disable() {
    }

    TaskController provideTaskController();

    default File getDataFolder() {
        return getStarter().getDataFolder();
    }

    default  <T extends OComponent> T findComponent(Class<T> clazz) {
        return (T) getEngine().getPluginComponentController().getComponents().stream().filter(comp -> comp.getClass() == clazz).findFirst().orElse(null);
    }
}
