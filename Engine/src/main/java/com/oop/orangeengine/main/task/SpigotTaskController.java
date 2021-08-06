package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.plugin.EngineBootstrap;
import com.oop.orangeengine.main.util.DisablePriority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

@Getter
public class SpigotTaskController implements TaskController {

    private EngineBootstrap owning;
    private Set<OTask> tasks = new HashSet<>();

    public SpigotTaskController(EngineBootstrap owning) {
        this.owning = owning;
        owning.onDisable(() -> tasks.forEach(OTask::cancel), DisablePriority.LAST);
    }

    @Override
    public OTask runTask(OTask task) {
        BukkitTask bukkitTask;
        if (task.isSync())
            bukkitTask = sync(task);

        else
            bukkitTask = async(task);

        if (bukkitTask != null) {
            task.setBukkitTask(bukkitTask);
            if (task.isRepeat() || task.getDelay() > 1)
                tasks.add(task);
        }

        return task;
    }

    private BukkitTask async(OTask task) {
        if (task.isRepeat())
            return Bukkit.getScheduler().runTaskTimerAsynchronously(owning.getStarter(), task.run(), 0, task.getDelayAsTicks());

        else if (task.getDelay() != -1)
            return Bukkit.getScheduler().runTaskLaterAsynchronously(owning.getStarter(), task.run(), task.getDelayAsTicks());

        else if (!isAsyncThread())
            return Bukkit.getScheduler().runTaskAsynchronously(owning.getStarter(), task.run());

        else {
            task.run().run();
            return null;
        }

    }

    private BukkitTask sync(OTask task) {
        if (task.isRepeat())
            return Bukkit.getScheduler().runTaskTimer(owning.getStarter(), task.run(), 0, task.getDelayAsTicks());

        else if (task.getDelay() != -1)
            return Bukkit.getScheduler().runTaskLater(owning.getStarter(), task.run(), task.getDelayAsTicks());

        else if (isAsyncThread())
            return Bukkit.getScheduler().runTask(owning.getStarter(), task.run());

        else {
            task.run().run();
            return null;
        }
    }

    private boolean isAsyncThread() {
        return !Thread.currentThread().getName().startsWith("Server Thread");
    }
}
