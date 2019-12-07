package com.oop.orangeengine.main.events;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.events.async.AsyncEvent;
import com.oop.orangeengine.main.events.async.EventData;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface AsyncEvents extends Listener, EventExecutor {

    List<AsyncEvents> registeredEvents = new ArrayList<>();

    static <T extends Event> AsyncEvents listen(
            Class<T> type,
            Consumer<T> listener
    ) {
        return listen(type, EventPriority.NORMAL, listener);
    }

    static <T extends Event> AsyncEvents listen(
            Class<T> type,
            EventPriority priority,
            Consumer<T> listener
    ) {

        final AsyncEvents events = ($, event) -> {

            if (type.isInstance(event)) {
                async(() -> listener.accept(type.cast(event)));
            }

        };

        Bukkit.getPluginManager().registerEvent(type, events, priority, events, Engine.getInstance().getOwning());
        registeredEvents.add(events);

        return events;
    }

    static <T extends Event> AsyncEvents listen(
            Class<T> type,
            Consumer<T> listener,
            Consumer<T> preAsync
    ) {

        final AsyncEvents events = ($, event) -> {
            if (type.isInstance(event)) {
                preAsync.accept(type.cast(event));
                async(() -> listener.accept(type.cast(event)));
            }
        };

        Bukkit.getPluginManager().registerEvent(type, events, EventPriority.NORMAL, events, Engine.getInstance().getOwning());
        registeredEvents.add(events);

        return events;
    }

    static <T extends Event> AsyncEvents listen(
            AsyncEvent<T> asyncEvent
    ) {

        final AsyncEvents events = ($, event) -> {
            if (asyncEvent.classType().isInstance(event)) {
                if (asyncEvent.cancelIf() != null && asyncEvent.cancelIf().test(asyncEvent.classType().cast(event)))
                    return;

                EventData data = new EventData();
                if (EventData.getDataTypes().containsKey(asyncEvent.classType()))
                    EventData.getDataType(asyncEvent.classType()).accept(asyncEvent.classType().cast(event), data);

                if (asyncEvent.preAsync() != null)
                    asyncEvent.preAsync().accept(asyncEvent.classType().cast(event), data);

                async(() -> {
                    if (asyncEvent.cancelIf() != null && asyncEvent.cancelIf().test(asyncEvent.classType().cast(event)))
                        return;

                    if (asyncEvent.async() != null)
                        asyncEvent.async().accept(asyncEvent.classType().cast(event), data);
                });

            }
        };

        if (asyncEvent.priority() != null)
            Bukkit.getPluginManager().registerEvent(asyncEvent.classType(), events, asyncEvent.priority(), events, Engine.getInstance().getOwning());

        else
            Bukkit.getPluginManager().registerEvent(asyncEvent.classType(), events, asyncEvent.priority(), events, Engine.getInstance().getOwning());

        registeredEvents.add(events);
        return events;
    }

    static void async(Runnable runnable) {
        StaticTask.getInstance().async(runnable);
    }

    static void unregister() {
        registeredEvents.forEach(HandlerList::unregisterAll);
    }

}
