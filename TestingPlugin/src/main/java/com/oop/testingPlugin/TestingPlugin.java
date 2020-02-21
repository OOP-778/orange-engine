package com.oop.testingPlugin;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            OConfiguration configuration = new OConfiguration(new OFile(getDataFolder(), "test.yml").createIfNotExists());
            YamlMessage.load(configuration, "test").send(event.getPlayer());
        });
    }

    void print(Object ob) {
        Bukkit.getConsoleSender().sendMessage(ob.toString());
    }

    @Override
    public ITaskController provideTaskController() {
        return new ClassicTaskController(this);
    }
}

