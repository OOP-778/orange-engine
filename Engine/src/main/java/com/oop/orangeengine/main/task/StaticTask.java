package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class StaticTask {
    private static StaticTask INSTANCE;

    public StaticTask() {
        INSTANCE = this;
    }

    public static StaticTask getInstance() {
        return INSTANCE;
    }

    public void runSyncThenAsync(Runnable sync, Runnable async) {
        if (checkForShutdown(sync, async)) return;
        sync(() -> {
            sync.run();
            async(async);
        });
    }

    public void ensureSync(Runnable runnable) {
        ensureSync(runnable, null);
    }

    public void ensureSync(Runnable runnable, Runnable callback) {
        if (checkForShutdown(runnable, callback)) return;
        Runnable finalRunnable = runnable;
        runnable = () -> {
            finalRunnable.run();
            if (callback != null)
                callback.run();
        };

        if (Thread.currentThread().getName().equalsIgnoreCase("Server Thread"))
            runnable.run();

        else
            sync(runnable);
    }

    public void ensureAsync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            async(() -> ensureAsync(runnable));

        runnable.run();
    }

    public void runAsyncThenSync(Runnable async, Runnable sync) {
        if (checkForShutdown(async, sync)) return;
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
        if (checkForShutdown(runnable)) return;
        new OTask().sync(false).runnable(runnable).execute();
    }

    public BukkitTask sync(Runnable runnable) {
        if (checkForShutdown(runnable)) return null;
        if (Thread.currentThread().getName().equalsIgnoreCase("Server Thread"))
            runnable.run();

        else
            return Bukkit.getScheduler().runTask(getEngine().getOwning(), runnable);

        return null;
    }

    public boolean checkForShutdown(Runnable... runnables) {
        if (getEngine().getOwning().isDisabling()) {
            for (Runnable runnable : runnables) {
                if (runnable == null) continue;
                runnable.run();
            }
            return true;
        }
        return false;
    }
}
