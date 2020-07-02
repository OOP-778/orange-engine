package com.oop.testingPlugin;

import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.TaskController;
import com.oop.orangeengine.message.impl.OChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            new OChatMessage("#FF5733&lHello &7> &bPlayer")
                    .send(event.getPlayer());
        });
    }

    void print(Object ob) {
        Bukkit.getConsoleSender().sendMessage(ob.toString());
    }

    @Override
    public TaskController provideTaskController() {
        return new ClassicTaskController(this);
    }
}

