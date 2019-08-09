package com.oop.orangeEngine.main;

import com.oop.orangeEngine.main.component.AEngineComponent;
import com.oop.orangeEngine.main.component.IEngineComponent;
import com.oop.orangeEngine.main.events.async.EventData;
import com.oop.orangeEngine.main.logger.OLogger;
import com.oop.orangeEngine.main.plugin.EnginePlugin;
import com.oop.orangeEngine.main.task.StaticTask;
import com.oop.orangeEngine.main.task.TaskController;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Engine {

    private static Engine instance;
    private EnginePlugin owning;
    private List<AEngineComponent> components = new ArrayList<>();
    private TaskController taskController;
    private OLogger logger;

    public Engine(EnginePlugin plugin) {
        instance = this;

        //Initialize plugin disable actions
        owning = plugin;
        owning.onDisable(() -> {

            components.forEach(IEngineComponent::onDisable);
            instance = null;

        });

        Cleaner cleaner = new Cleaner();
        cleaner.registerClass(StaticTask.class);
        cleaner.registerClass(EventData.class);

        new StaticTask(this);

        //Initialize task controller
        taskController = new TaskController(owning);
        logger = new OLogger(owning);

        ClassLoader.load();

    }

    public static Engine getInstance() {
        return instance;
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
