package com.oop.orangeEngine.eventsSubscription.subscription;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface SubEvent extends Listener, EventExecutor {
    List<SubEvent> registeredEvents = new ArrayList();

    static <T extends Event> SubEvent listen(Class<T> type, EventPriority priority, Consumer<T> listener, JavaPlugin plugin) {
        SubEvent events = ($, event) -> {

            if (type.isInstance(event)) {
                listener.accept(type.cast(event));
            }

        };

        Bukkit.getPluginManager().registerEvent(type, events, priority, events, plugin);
        registeredEvents.add(events);

        return events;
    }

    static void unregister(SubEvent subEvent) {
        HandlerList.unregisterAll(subEvent);
        registeredEvents.remove(subEvent);
    }

    static void unregisterAll() {
        registeredEvents.forEach(HandlerList::unregisterAll);
    }
}