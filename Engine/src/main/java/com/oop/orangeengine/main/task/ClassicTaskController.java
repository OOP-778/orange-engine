package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.plugin.EngineBootstrap;
import com.oop.orangeengine.main.util.DisablePriority;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassicTaskController implements TaskController {

    private Set<OTask> asyncTasks = new HashSet<>();
    private ScheduledThreadPoolExecutor executor;
    private EngineBootstrap plugin;

    private int threadsCount = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    public ClassicTaskController(EngineBootstrap plugin) {
        this.plugin = plugin;
        plugin.onDisable(() -> {
            for (OTask asyncTask : asyncTasks)
                asyncTask.cancel();

            asyncTasks.clear();
            try {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, DisablePriority.LAST);

        executor = new ScheduledThreadPoolExecutor(threadsCount, runnable -> {
            String name = "OrangeEngine-Executor-" + ThreadLocalRandom.current().nextInt(100);
            return new Thread(runnable, name);
        });
        executor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public OTask runTask(OTask task) {
        Object nativeTask;

        if (task.isSync())
            nativeTask = sync(task);

        else
            nativeTask = async(task);

        if (nativeTask == null) return task;

        if (nativeTask instanceof ScheduledFuture)
            task.setScheduledFuture((ScheduledFuture<?>) nativeTask);

        else if (nativeTask instanceof BukkitTask)
            task.setBukkitTask((BukkitTask) nativeTask);

        return task;
    }

    private Object async(OTask task) {
        if (getEngine().getOwning().isDisabling()) {
            task.run();
            return null;
        }

        if (threadsCount <= executor.getQueue().size() && !getEngine().getOwning().isDisabling()) {
            if (task.isRepeat())
                return Bukkit.getScheduler().runTaskTimerAsynchronously(getEngine().getOwning().getStarter(), task.run(), 0, task.getDelayAsTicks());

            else if (task.getDelay() != -1)
                return Bukkit.getScheduler().runTaskLater(getEngine().getOwning().getStarter(), task.run(), task.getDelayAsTicks());

            else
                return Bukkit.getScheduler().runTaskAsynchronously(getEngine().getOwning().getStarter(), task.run());
        }

        if (task.isRepeat())
            return executor.scheduleAtFixedRate(task.run(), 0, task.getDelay(), TimeUnit.MILLISECONDS);

        else if (task.getDelay() != -1)
            return executor.schedule(task.run(), task.getDelay(), TimeUnit.MILLISECONDS);

        else
            executor.execute(task.run());

        return null;
    }

    private BukkitTask sync(OTask task) {
        if (getEngine().getOwning().isDisabling()) {
            task.run();
            return null;
        }

        if (task.isRepeat())
            return Bukkit.getScheduler().runTaskTimer(plugin.getStarter(), task.run(), 0, task.getDelayAsTicks());

        else if (task.getDelay() != -1)
            return Bukkit.getScheduler().runTaskLater(plugin.getStarter(), task.run(), task.getDelayAsTicks());

        else {
            if (Thread.currentThread().getName().equalsIgnoreCase("Server Thread"))
                task.run();
            else
               return Bukkit.getScheduler().runTask(plugin.getStarter(), task.run());
        }
        return null;
    }
}
