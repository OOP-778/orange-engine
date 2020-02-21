package com.oop.orangeengine.main.player;

import com.google.common.collect.Sets;
import com.oop.orangeengine.main.storage.Storegable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
@RequiredArgsConstructor (staticName = "of", access = AccessLevel.PROTECTED)
public class OPlayer extends Storegable {

    private final UUID uuid;

    private Set<Class<? extends Event>> allowedEventsWhileDisabled = Sets.newConcurrentHashSet();
    private boolean isEventsDisabled = false;

    private boolean allowedToChat = true;
    private boolean allowedToReceive = true;

    private Map<Class<? extends Event>, Consumer<Event>> registeredConsumers = new ConcurrentHashMap<>();

    public <T extends Event> void registerEventConsumer(Class<T> eventClass, Consumer<T> consumer) {
        registeredConsumers.put(eventClass, (Consumer<Event>) consumer);
    }

    public void unregisterEventConsumer(Class<? extends Event> eventClass) {
        registeredConsumers.remove(eventClass);
    }
}
