package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class StaticTask {

    private JavaPlugin owning;
    private static StaticTask INSTANCE;

    public StaticTask(Engine engine) {
        this.owning = engine.getOwning();
        INSTANCE = this;
    }

    public static StaticTask getInstance() {
        return INSTANCE;
    }

    public BukkitTask runSyncThenAsync(Runnable sync, Runnable async) {
       return sync(() -> {
            sync.run();
            async(async);
        });
    }

    public BukkitTask runAsyncThenSync(Runnable async, Runnable sync) {
        return async(() -> {
            async.run();
            sync(sync);
        });
    }

    public BukkitTask async(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(owning, runnable);
    }

    public BukkitTask sync(Runnable runnable) {
        return Bukkit.getScheduler().runTask(owning, runnable);
    }

}
