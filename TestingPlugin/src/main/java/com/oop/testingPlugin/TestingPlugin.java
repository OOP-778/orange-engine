package com.oop.testingPlugin;

import com.oop.orangeengine.hologram.Hologram;
import com.oop.orangeengine.hologram.HologramController;
import com.oop.orangeengine.hologram.line.HologramItem;
import com.oop.orangeengine.item.custom.OSkull;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.task.TaskController;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicReference;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        HologramController hologramController = new HologramController(this, 10);
        AtomicReference<Hologram> holo = new AtomicReference<>(null);
        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            if (holo.get() != null) {
                holo.get().getBaseLocation().set(event.getPlayer().getLocation().clone().add(0, 2, 0));
                return;
            }

            holo.set(new Hologram(event.getPlayer().getLocation().add(0, 2, 0)));
            HologramItem hologramItem = new HologramItem(new ItemStack(Material.DIAMOND_HELMET));
            holo.get().addLine("&c&lHello");
            holo.get().addLine(hologramItem);

//            new OTask()
//                    .delay(TimeUnit.SECONDS, 1)
//                    .repeat(true)
//                    .runnable(() -> hologramItem.setLocation(hologramItem.getLocation().current().clone().add(0, 0.1, 0)))
//                    .execute();

            hologramController.registerHologram(holo.get());
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

