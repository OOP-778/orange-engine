package com.oop.testingPlugin;

import com.oop.orangeengine.main.plugin.EngineBootstrap;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.TaskController;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestingBootstrap extends JavaPlugin implements EngineBootstrap {

    @Override
    public JavaPlugin getStarter() {
        return this;
    }

    @Override
    public void enable() {
    }

    void print(Object ob) {
        Bukkit.getConsoleSender().sendMessage(ob.toString());
    }

    @Override
    public TaskController provideTaskController() {
        return new ClassicTaskController(this);
    }
}

