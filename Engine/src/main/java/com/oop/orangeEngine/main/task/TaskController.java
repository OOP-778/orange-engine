package com.oop.orangeEngine.main.task;

import com.oop.orangeEngine.main.plugin.EnginePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class TaskController implements ITaskController {

    private EnginePlugin owning;
    public TaskController(EnginePlugin owning) {
        this.owning = owning;
    }

    @Override
    public OTask runTask(OTask task) {
        BukkitTask bukkitTask;
        if(task.isSync())
            bukkitTask = sync(task);

        else
            bukkitTask = async(task);

        task.setBukkitTask(bukkitTask);
        return task;

    }

    private BukkitTask async(OTask task){
        if(task.isRepeat())
            return Bukkit.getScheduler().runTaskTimerAsynchronously(owning, task.run(), 0, task.getDelayAsTicks());

        else
            if(task.getDelay() != -1)
                return Bukkit.getScheduler().runTaskLaterAsynchronously(owning, task.run(), task.getDelayAsTicks());

        else
            return Bukkit.getScheduler().runTaskAsynchronously(owning, task.run());

    }

    private BukkitTask sync(OTask task) {
        if(task.isRepeat())
            return Bukkit.getScheduler().runTaskTimer(owning, task.run(), 0, task.getDelayAsTicks());

        else
            if(task.getDelay() != -1)
                return Bukkit.getScheduler().runTaskLater(owning, task.run(), task.getDelayAsTicks());

            else
                return Bukkit.getScheduler().runTask(owning, task.run());

    }
}
