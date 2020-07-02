package com.oop.orangeengine.main.task;

import lombok.SneakyThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.oop.orangeengine.main.Engine.getEngine;

public interface TaskController {

    default OTask runTask(Runnable runnable) {
        return runTask(runnable::run);
    }

    OTask runTask(OTask task);

    default OTask scheduleDelayed(Consumer<OTask> consumer, TimeUnit unit, long time, boolean sync) {
        return runTask(
                new OTask().
                        sync(sync).
                        delay(unit, time).
                        consumer(consumer)
        );

    }

    default OTask scheduleDelayed(Consumer<OTask> consumer, TimeUnit unit, long time) {
        return scheduleDelayed(consumer, unit, time, true);
    }

    default OTask scheduleDelayed(Runnable runnable, TimeUnit unit, long time) {
        return scheduleDelayed((task) -> runnable.run(), unit, time);
    }

    default OTask scheduleDelayed(Runnable runnable, TimeUnit unit, long time, boolean sync) {
        return scheduleDelayed((task) -> runnable.run(), unit, time, sync);
    }

    default OTask scheduleRepeated(Consumer<OTask> consumer, TimeUnit unit, long time, boolean sync) {
        return runTask(
                new OTask().
                        sync(sync).
                        delay(unit, time).
                        consumer(consumer)
        );

    }

    default OTask scheduleRepeated(Consumer<OTask> consumer, TimeUnit unit, long time) {
        return scheduleRepeated(consumer, unit, time, true);
    }

    default OTask scheduleRepeated(Runnable runnable, TimeUnit unit, long time) {
        return scheduleRepeated((task) -> runnable.run(), unit, time);
    }

    default OTask scheduleRepeated(Runnable runnable, TimeUnit unit, long time, boolean sync) {
        return scheduleRepeated((task) -> runnable.run(), unit, time, sync);
    }

    default OTask scheduleNow(Consumer<OTask> consumer, boolean sync) {
        return runTask(
                new OTask().
                        sync(sync).
                        consumer(consumer)
        );
    }

    default OTask scheduleNowAsync(Consumer<OTask> consumer) {
        return scheduleNow(consumer, false);
    }

    default OTask scheduleNowSync(Consumer<OTask> consumer) {
        return scheduleNow(consumer, true);
    }

    void trackTask(OTask task);

    void untrackTask(OTask task);

    Map<OTask, Long> getTrackingTasks();

    default void checkTasks() {
        getTrackingTasks().forEach((task, timeStarted) -> {
            long seconds = Duration.between(Instant.ofEpochMilli(timeStarted), Instant.now()).getSeconds();
            if (seconds > 20) {
                getEngine().getLogger().printWarning("A thread {} been hung by a task for {} seconds.", task.getRunningThread().getName(), seconds);

                getEngine().getLogger().printWarning("Creation Stack Trace...");
                for (StackTraceElement stackTraceElement : task.getCreationStackTrace())
                    getEngine().getLogger().printWarning("- " + stackTraceElement.getClassName() + "#" + stackTraceElement.getMethodName() + " at " + stackTraceElement.getLineNumber());

                getEngine().getLogger().printWarning("Current thread Stack Trace...");
                StackTraceElement[] stackTrace = task.getRunningThread().getStackTrace();
                for (StackTraceElement stackTraceElement : stackTrace)
                    getEngine().getLogger().printWarning("- " + stackTraceElement.getClassName() + "#" + stackTraceElement.getMethodName() + " at " + stackTraceElement.getLineNumber());

                task.getRunningThread().interrupt();
                getTrackingTasks().remove(task);
            }
        });
    }

    default void loadTask() {
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Thread thread = new Thread("OrangeEngine-Task-Tracker") {
            @SneakyThrows
            @Override
            public void run() {
                while (!shutdown.get()) {
                    checkTasks();
                    sleep(100);
                }
            }
        };

        thread.start();
        getEngine().getOwning().onDisable(() -> shutdown.set(true));
    }
}
