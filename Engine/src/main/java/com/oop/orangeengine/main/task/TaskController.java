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
}
