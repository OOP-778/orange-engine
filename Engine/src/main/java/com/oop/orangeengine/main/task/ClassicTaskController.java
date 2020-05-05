package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.util.DisablePriority;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class ClassicTaskController implements ITaskController {

    private Set<OTask> asyncTasks = new HashSet<>();
    private ScheduledExecutorService executor;
    private EnginePlugin plugin;

    public ClassicTaskController(EnginePlugin plugin) {
        this.plugin = plugin;
        plugin.onDisable(() -> {
            for (OTask asyncTask : asyncTasks) {
                asyncTask.cancel();
            }
            asyncTasks.clear();
            try {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, DisablePriority.LAST);
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1, runnable -> new Thread(runnable, "OrangeEngine-Executor-" + ThreadLocalRandom.current().nextInt(100)));
        threadPoolExecutor.setRemoveOnCancelPolicy(true);
        executor = Executors.unconfigurableScheduledExecutorService(threadPoolExecutor);
    }

    @Override
    public OTask runTask(OTask task) {
        BukkitTask bukkitTask = null;
        ScheduledFuture<?> future = null;

        if (task.isSync())
            bukkitTask = sync(task);

        else
            future = async(task);

        if (bukkitTask != null) {
            task.setBukkitTask(bukkitTask);

        } else if (future != null)
            task.setScheduledFuture(future);

        return task;
    }

    private ScheduledFuture<?> async(OTask task) {
        if (task.isRepeat())
            return executor.scheduleAtFixedRate(task.run(), 0, task.getDelay(), TimeUnit.MILLISECONDS);

        else if (task.getDelay() != -1)
            return executor.scheduleWithFixedDelay(task.run(), task.getDelay(), task.getDelay(), TimeUnit.MILLISECONDS);

        else
            executor.execute(task.run());

        return null;
    }

    private BukkitTask sync(OTask task) {
        if (task.isRepeat())
            return Bukkit.getScheduler().runTaskTimer(plugin, task.run(), 0, task.getDelayAsTicks());

        else if (task.getDelay() != -1)
            return Bukkit.getScheduler().runTaskLater(plugin, task.run(), task.getDelayAsTicks());

        else
            return Bukkit.getScheduler().runTask(plugin, task.run());
    }
}
