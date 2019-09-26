package com.oop.orangeengine.main.util;

import com.oop.orangeengine.main.task.StaticTask;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class OFuture<T> {

    private AtomicReference<T> received = new AtomicReference<>(null);
    private Consumer<T> whenReceived;

    public void complete(T object) {
        this.received.set(object);
        if (whenReceived != null)
            whenReceived.accept(object);
    }

    public OFuture<T> whenReceivedSync(Consumer<T> consumer) {
        this.whenReceived = t -> StaticTask.getInstance().sync(() -> consumer.accept(t));
        return this;
    }

    public OFuture<T> whenReceivedAsync(Consumer<T> consumer) {
        this.whenReceived = t -> StaticTask.getInstance().async(() -> consumer.accept(t));
        return this;
    }

}
