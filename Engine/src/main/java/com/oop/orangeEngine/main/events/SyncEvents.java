package com.oop.orangeEngine.main.events;

import com.oop.orangeEngine.main.Engine;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface SyncEvents extends Listener, EventExecutor {

    List<SyncEvents> registeredEvents = new ArrayList<>();

    static <T extends Event> SyncEvents listen(
            Class<T> type,
            Consumer<T> listener
    ) {

        final SyncEvents events = ($, event) -> {
            if (type.isInstance(event)) {
                listener.accept(type.cast(event));
            }
        };
        Bukkit.getPluginManager().registerEvent(type, events, EventPriority.NORMAL, events, Engine.getInstance().getOwning());
        registeredEvents.add(events);
        return events;
    }

    static <T extends Event> SyncEvents listen(
            Class<T> type,
            EventPriority priority,
            Consumer<T> listener
    ) {

        final SyncEvents events = ($, event) -> {
            if (type.isInstance(event)) {
                listener.accept(type.cast(event));
            }
        };
        Bukkit.getPluginManager().registerEvent(type, events, priority, events, Engine.getInstance().getOwning());
        registeredEvents.add(events);
        return events;
    }

    static void unregister() {
        registeredEvents.forEach(HandlerList::unregisterAll);
    }

}

