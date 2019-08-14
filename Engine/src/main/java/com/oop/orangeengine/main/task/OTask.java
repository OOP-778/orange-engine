package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.main.util.OptionalConsumer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class OTask extends Storegable {

    @Setter
    private BukkitTask bukkitTask;

    private Consumer<OTask> consumer;
    private Consumer<OTask> whenFinished;
    private Predicate<OTask> stopIf;
    private long delay = -1;
    private boolean repeat = false;
    private boolean sync = true;
    private int runTimes = -1;

    protected Runnable run() {

        return () -> {

            //Check for run times
            OptionalConsumer<Integer> runned = grab("runned");
            if (runned.isPresent() && (int) runned.get() == runTimes) {
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

            //Run
            try {
                consumer.accept(this);
            } catch (Exception ex) {
                Engine.getInstance().getLogger().error(ex);
            }
            if (runTimes != -1)
                storeIfPresentUpdate("runned", 1, Integer::sum);

        };

    }

    public int getTaskId() {
        return bukkitTask.getTaskId();
    }

    public Plugin getOwner() {
        return bukkitTask.getOwner();
    }

    public boolean isSync() {
        return sync;
    }

    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }

    public void cancel() {
        bukkitTask.cancel();
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

    public OTask whenFinished(Consumer<OTask> whenFinished) {
        this.whenFinished = whenFinished;
        return this;
    }

    public OTask whenFinished(Runnable whenFinished) {
        this.whenFinished = (task) -> whenFinished.run();
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
