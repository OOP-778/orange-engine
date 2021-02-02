package com.oop.testingPlugin;

import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.TaskController;
import org.bukkit.Bukkit;

public class TestingPlugin extends EnginePlugin {

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

