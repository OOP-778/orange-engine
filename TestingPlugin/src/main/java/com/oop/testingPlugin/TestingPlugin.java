package com.oop.testingPlugin;

import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            StaticTask.getInstance().sync(() -> {
                List<FallingBlock> blocks = new ArrayList<>();

                Location baseLocation = event.getPlayer().getLocation().add(0, -1, 0);
                for (int i = 0; i < 20; i++) {
                    baseLocation.add(new Vector(1, 0, 0));
                }

                blocks.forEach(block -> block.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(1.5f)));

                new OTask()
                        .repeat(true)
                        .runTimes(100)
                        .delay(500)
                        .sync(false)
                        .runnable(() -> {
                            blocks.forEach(block -> block.setVelocity(block.getVelocity().add(new Vector(0, 1, 0))));
                        })
                        .execute();
            });
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

