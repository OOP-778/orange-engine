package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.OQueue;

import java.util.concurrent.TimeUnit;

public class AsyncQueue {

    private OQueue<Runnable> runnables = new OQueue<>();

    private ODatabase database;
    public AsyncQueue(ODatabase database) {
        new OTask()
                .repeat(true)
                .sync(false)
                .delay(TimeUnit.MILLISECONDS, 10)
                .runnable(() -> {
                    if (runnables.isEmpty()) return;

                    Runnable runnable = runnables.poll();
                    runnable.run();
                })
                .execute();
    }

    public void add(Runnable runnable) {
        this.runnables.add(runnable);
    }

}
