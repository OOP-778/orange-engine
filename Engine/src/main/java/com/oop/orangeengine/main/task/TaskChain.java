package com.oop.orangeengine.main.task;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.storage.Storegable;

import java.util.function.Consumer;

public class TaskChain extends Storegable {

    private Consumer<TaskChain> consumer;
    private OTask task;
    private Consumer<TaskChain> next;

    public TaskChain start(Consumer<TaskChain> consumer, boolean async) {
        this.consumer = (task) -> {

            consumer.accept(task);
            next();

        };

        task = Engine.getInstance().getTaskController().scheduleNow((t) -> consumer.accept(this), !async);
        return this;
    }

    private TaskChain whenFinished(Consumer<TaskChain> consumer, boolean async) {
        this.next = (t) -> task = Engine.getInstance().getTaskController().scheduleNow((task) -> {

            consumer.accept(t);
            next();

        }, !async);
        return this;

    }

    public TaskChain whenFinishedAsync(Consumer<TaskChain> consumer) {
        return whenFinished(consumer, true);
    }

    public TaskChain whenFinishedSync(Consumer<TaskChain> consumer) {
        return whenFinished(consumer, false);
    }

    private void next() {
        if (next != null)
            next.accept(this);
    }

    public OTask getTask() {
        return task;
    }
}
