package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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

    public void runSyncThenAsync(Runnable sync, Runnable async) {
        sync(() -> {
            sync.run();
            async(async);
        });
    }

    public void runAsyncThenSync(Runnable async, Runnable sync) {
        async(() -> {
            async.run();
            sync(sync);
        });
    }

    public <T> CompletableFuture<T> gatherFromSync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        sync(() -> future.complete(supplier.get()));
        return future;
    }

    public void async(Runnable runnable) {
        new OTask().sync(false).runnable(runnable).execute();
    }

    public BukkitTask sync(Runnable runnable) {
        return Bukkit.getScheduler().runTask(owning, runnable);
    }

}
