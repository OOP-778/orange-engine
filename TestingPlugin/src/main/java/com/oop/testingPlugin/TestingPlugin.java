package com.oop.testingPlugin;

import com.oop.orangeengine.database.util.Tester;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        DatabaseController controller = new DatabaseController(this);

        SyncEvents.listen(EntityDeathEvent.class, event -> {
            Player killer = event.getEntity().getKiller();
            if (killer == null) return;

            StatsPlayer orInsert = controller.getHolder().getOrInsert(killer);
            orInsert.setKills(orInsert.getKills() + 1);
            killer.sendMessage("Your kills now are " + orInsert.getKills());

            Tester.t("Save", () -> orInsert.save(false));
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

