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
            if (asyncEvent.getClassType().isInstance(event)) {

                EventData data = new EventData();
                if (EventData.getDataTypes().containsKey(asyncEvent.getClassType()))
                    EventData.getDataType(asyncEvent.getClassType()).accept(asyncEvent.getClassType().cast(event), data);

                if (asyncEvent.getPreAsync() != null)
                    asyncEvent.getPreAsync().accept(asyncEvent.getClassType().cast(event), data);

                async(() -> {
                    if (asyncEvent.getAsync() != null)
                        asyncEvent.getAsync().accept(asyncEvent.getClassType().cast(event), data);
                });

            }
        };

        if (asyncEvent.getEventPriority() != null)
            Bukkit.getPluginManager().registerEvent(asyncEvent.getClassType(), events, asyncEvent.getEventPriority(), events, Engine.getInstance().getOwning());
        else
            Bukkit.getPluginManager().registerEvent(asyncEvent.getClassType(), events, asyncEvent.getEventPriority(), events, Engine.getInstance().getOwning());

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
