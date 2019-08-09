package com.oop.orangeEngine.main.events.async;

import com.oop.orangeEngine.main.events.AsyncEvents;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.BiConsumer;

public class AsyncEvent<E extends Event> {

    private BiConsumer<E, EventData> preAsync = null;
    private BiConsumer<E, EventData> async = null;
    private EventPriority eventPriority = EventPriority.NORMAL;

    private Class<E> classType;

    public AsyncEvent(Class<E> classType) {
        this.classType = classType;
    }

    public AsyncEvent<E> preAsync(BiConsumer<E, EventData> listener) {

        this.preAsync = listener;
        return this;

    }

    public AsyncEvent<E> async(BiConsumer<E, EventData> listener) {

        this.async = listener;
        return this;

    }

    public BiConsumer<E, EventData> getAsync() {
        return async;
    }

    public AsyncEvent<E> priority(EventPriority eventPriority) {
        this.eventPriority = eventPriority;
        return this;
    }

    public EventPriority getEventPriority() {
        return eventPriority;
    }

    public BiConsumer<E, EventData> getPreAsync() {
        return preAsync;
    }

    public Class<E> getClassType() {
        return classType;
    }

    public void register() {
        AsyncEvents.listen(this);
    }
}
