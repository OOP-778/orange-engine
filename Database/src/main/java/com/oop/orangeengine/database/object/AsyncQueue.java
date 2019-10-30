package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.OQueue;

import java.util.concurrent.TimeUnit;

public class AsyncQueue {

    private OQueue<Runnable> runnables = new OQueue<>();

    private ODatabase database;
    private boolean isBukkit = true;

    public AsyncQueue(ODatabase database) {

        if (Engine.getInstance() != null && Engine.getInstance().getTaskController() != null) {
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
        } else isBukkit = false;
    }

    public void add(Runnable runnable) {
        if (isBukkit)
            this.runnables.add(runnable);

        else
            runnable.run();
    }

}
