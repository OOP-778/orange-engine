package com.oop.orangeengine.main.plugin;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.task.TaskController;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

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
        disable();
        runOnDisable.forEach(Runnable::run);
    }

    public void onDisable(Runnable runnable) {
        this.runOnDisable.add(runnable);
    }

    public abstract void enable();
    public void disable() {}

}
