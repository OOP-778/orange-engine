package com.oop.orangeengine.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.component.IEngineComponent;
import com.oop.orangeengine.main.events.async.EventData;
import com.oop.orangeengine.main.gson.BukkitAdapter;
import com.oop.orangeengine.main.gson.UpdateableAdapterFactory;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.task.SpigotTaskController;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Engine {

    private static Engine instance;
    private EnginePlugin owning;
    private List<AEngineComponent> components = new ArrayList<>();
    private ITaskController taskController;
    private OLogger logger;
    private final Gson gson;

    public Engine(EnginePlugin plugin) {
        instance = this;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();

        new BukkitAdapter(gsonBuilder);
        new UpdateableAdapterFactory(gsonBuilder);
        gson = gsonBuilder.create();

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

        taskController = plugin.provideTaskController();
        logger = new OLogger(owning);

        ClassLoader.load();
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
