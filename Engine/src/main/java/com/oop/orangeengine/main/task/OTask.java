package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.main.util.OptionalConsumer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class OTask extends Storegable {

    // If spigot
    @Setter
    private BukkitTask bukkitTask;

    // Classic
    @Setter
    private ScheduledFuture<?> scheduledFuture;

    private Consumer<OTask> consumer;
    private Consumer<OTask> whenFinished;
    private Predicate<OTask> stopIf;
    private long delay = -1;
    private boolean repeat = false;
    private boolean sync = true;
    private int runTimes = -1;
    private boolean cancelled = false;

    private Thread runningThread;

    protected Runnable run() {
        return () -> {
            if (cancelled) return;
            if (getEngine().getOwning().isDisabling()) return;

            //Check for run times
            OptionalConsumer<Integer> runned = grab("runned");
            if (runned.isPresent() && runned.get() == runTimes) {
                cancel();

                if (whenFinished != null)
                    whenFinished.accept(this);

                return;
            }

            //Check if stop if is
            if (getStopIf() != null && getStopIf().test(this)) {
                cancel();

                if (whenFinished != null)
                    whenFinished.accept(this);

                return;
            }

            runningThread = Thread.currentThread();

            //Run
            try {
                if (consumer != null)
                    consumer.accept(this);
            } catch (Exception ex) {
                Engine.getInstance().getLogger().printError("An error was caught in a task.");
                Engine.getInstance().getLogger().error(ex);
            }

            if (runTimes != -1)
                storeIfPresentUpdate("runned", 1, Integer::sum);
        };

    }

    public int getTaskId() {
        return bukkitTask == null ? -1 : bukkitTask.getTaskId();
    }

    public Plugin getOwner() {
        return getEngine().getOwning().getStarter();
    }

    public boolean isSync() {
        return sync;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }

        if (scheduledFuture != null)
            scheduledFuture.cancel(true);
    }

    public OTask runnable(Runnable runnable) {
        this.consumer = (task) -> runnable.run();
        return this;
    }

    public OTask consumer(Consumer<OTask> consumer) {
        this.consumer = consumer;
        return this;
    }

    public OTask delay(long time) {
        this.delay = time;
        return this;
    }

    public OTask delay(TimeUnit unit, long time) {
        this.delay = unit.toMillis(time);
        return this;
    }

    public OTask delay(TimeUnit unit, int time) {
        this.delay = unit.toMillis(time);
        return this;
    }

    public OTask delay(int time) {
        this.delay = time;
        return this;
    }

    public OTask repeat(boolean repeat) {
        this.repeat = repeat;
        return this;
    }

    public OTask stopFor(TimeUnit unit, long time) {
        cancel();
        new OTask()
                .delay(unit, time)
                .runnable(this::execute)
                .execute();

        return this;
    }

    public OTask stopFor(long milis) {
        return stopFor(TimeUnit.MILLISECONDS, milis);
    }

    public OTask whenFinished(Consumer<OTask> whenFinished) {
        return whenFinished(whenFinished, true);
    }

    public OTask whenFinished(Runnable whenFinished) {
        return whenFinished((task) -> whenFinished.run(), true);
    }

    public OTask whenFinished(Consumer<OTask> whenFinished, boolean sync) {
        this.whenFinished = (task) -> {
            if (sync)
                StaticTask.getInstance().sync(() -> whenFinished.accept(this));

            else
                StaticTask.getInstance().async(() -> whenFinished.accept(task));
        };
        return this;
    }

    public OTask sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public OTask stopIf(Predicate<OTask> stopIf) {
        this.stopIf = stopIf;
        return this;
    }

    public OTask runTimes(int runTimes) {
        this.runTimes = runTimes;
        return this;
    }

    public long getDelayAsTicks() {
        return Math.round(getDelay() / 1000 * 20);
    }

    public OTask execute() {
        Engine.getInstance().getTaskController().runTask(this);
        return this;
    }

}
