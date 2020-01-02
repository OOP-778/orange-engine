package com.oop.orangeengine.main.plugin;

import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.main.util.DisablePriority;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.task.SpigotTaskController;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class EnginePlugin extends JavaPlugin {

    private Map<DisablePriority, Runnable> onDisableRun = new HashMap<>();
    private Engine engine;
    private OLogger oLogger;

    private PluginComponentController pluginComponentController = new PluginComponentController();

    @Override
    public void onEnable() {

        //Init Defaults
        engine = new Engine(this);
        oLogger = engine.getLogger();

        //Init data folder
        if(!getDataFolder().exists())
            getDataFolder().mkdirs();

        //Enable plugin
        enable();
    }

    @Override
    public void onDisable() {
        disable();

        // Disable tasks
        onDisableRun.entrySet().stream()
                .sorted(Comparator.comparing(t -> t.getKey().getOrder()))
                .forEach(ds -> ds.getValue().run());

    }

    public void onDisable(Runnable runnable) {
        this.onDisableRun.put(DisablePriority.MIDDLE, runnable);
    }

    public void onDisable(Runnable runnable, DisablePriority priority) {
        this.onDisableRun.put(priority, runnable);
    }

    public ITaskController getTaskController() {
        return getEngine().getTaskController();
    }

    public abstract void enable();
    public void disable() {}

    public abstract ITaskController provideTaskController();

    public <T extends OComponent> T findComponent(Class<T> clazz) {
        return (T) pluginComponentController.getComponents().stream().filter(comp -> comp.getClass() == clazz).findFirst().orElse(null);
    }
}
