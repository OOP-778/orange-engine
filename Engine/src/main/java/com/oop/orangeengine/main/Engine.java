package com.oop.orangeengine.main;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.component.IEngineComponent;
import com.oop.orangeengine.main.events.async.EventData;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class Engine {

    public static boolean obfuscatorMode = false;
    private static Engine instance;
    private EnginePlugin owning;
    private List<AEngineComponent> components = new ArrayList<>();
    private ITaskController taskController;
    private OLogger logger;

    public Engine(EnginePlugin plugin) {
        instance = this;

        // Initialize plugin disable actions
        owning = plugin;
        owning.onDisable(() -> {
            components.forEach(IEngineComponent::onDisable);
            instance = null;
        });

        Cleaner cleaner = new Cleaner();
        cleaner.registerClass(StaticTask.class);
        cleaner.registerClass(EventData.class);

        new StaticTask(this);

        taskController = plugin.provideTaskController();
        logger = new OLogger(owning);

        ClassLoader.load(plugin.loader());
        Logger.getLogger("NBTAPI").setLevel(Level.OFF);
    }

    public static Engine getInstance() {
        return instance;
    }

    public static Engine getEngine() {
        return getInstance();
    }

    public void initComponent(AEngineComponent component) {
        this.components.add(component);
        component.onEnable();
        owning.onDisable(component::onDisable);
    }

    public <T extends AEngineComponent> T findComponentByClass(Class<T> clazz) {
        return (T) components.stream().
                filter(component -> component.getClass() == clazz).
                findFirst().
                orElse(null);
    }

    public AEngineComponent findComponentByName(String name) {
        return components.stream().
                filter(component -> component.getName().equalsIgnoreCase(name)).
                findFirst().
                orElse(null);

    }

    public void onDisable() {
        instance = null;
    }

}
