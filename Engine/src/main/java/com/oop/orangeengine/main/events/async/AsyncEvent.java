package com.oop.orangeengine.main.events.async;

import com.oop.orangeengine.main.events.AsyncEvents;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class AsyncEvent<E extends Event> {

    private BiConsumer<E, EventData> preAsync = null;
    private BiConsumer<E, EventData> async = null;
    private EventPriority priority = EventPriority.NORMAL;
    private Predicate<E> cancelIf;

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

    public AsyncEvent<E> priority(EventPriority eventPriority) {
        this.priority = eventPriority;
        return this;
    }


    public void register() {
        AsyncEvents.listen(this);
    }
}
