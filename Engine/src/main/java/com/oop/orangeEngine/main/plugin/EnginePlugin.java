package com.oop.orangeEngine.main.plugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import com.oop.orangeEngine.main.Engine;
import com.oop.orangeEngine.main.logger.OLogger;
import com.oop.orangeEngine.main.task.TaskController;

import java.util.LinkedList;
import java.util.List;

@Getter
public abstract class EnginePlugin extends JavaPlugin {

    private List<Runnable> runOnDisable = new LinkedList<>();
    private Engine engine;
    private OLogger oLogger;
    private TaskController taskController;

    @Override
    public void onEnable() {

        //Init Defaults
        engine = new Engine(this);
        oLogger = engine.getLogger();
        taskController = engine.getTaskController();

        //Init data folder
        if(!getDataFolder().exists())
            getDataFolder().mkdirs();

        //Enable plugin
        enable();

    }

    @Override
    public void onDisable() {
        runOnDisable.forEach(Runnable::run);
        disable();
    }

    public void onDisable(Runnable runnable) {
        this.runOnDisable.add(runnable);
    }

    public abstract void enable();
    public void disable() {}

}
