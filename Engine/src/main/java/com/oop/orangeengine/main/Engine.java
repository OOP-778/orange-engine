package com.oop.orangeengine.main;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.component.IEngineComponent;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.plugin.EngineBootstrap;
import com.oop.orangeengine.main.plugin.PluginComponentController;
import com.oop.orangeengine.main.task.TaskController;
import com.oop.orangeengine.main.util.DisablePriority;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class Engine {
    private static Engine instance;
    private final PluginComponentController pluginComponentController = new PluginComponentController();
    private List<AEngineComponent> components = new ArrayList<>();
    private TaskController taskController;
    private OLogger logger;
    private final Map<DisablePriority, Runnable> onDisableRun = new HashMap<>();
    private EngineBootstrap owning;
    @Setter
    private boolean disabling = false;

    public Engine(EngineBootstrap plugin) {
        instance = this;

        // Initialize plugin disable action
        owning = plugin;
        owning.onDisable(() -> {
            components.forEach(IEngineComponent::onDisable);
            instance = null;

            for (BukkitTask pendingTask : new HashSet<>(Bukkit.getScheduler().getPendingTasks())) {
                if (pendingTask.getOwner().getName().equalsIgnoreCase(owning.getName())) {
                    pendingTask.cancel();
                }
            }
        });

        taskController = plugin.provideTaskController();

        logger = new OLogger(owning);
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
}
