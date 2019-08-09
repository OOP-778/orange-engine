package com.oop.orangeEngine.main.player;

import com.oop.orangeEngine.main.storage.Storegable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
@RequiredArgsConstructor (staticName = "of", access = AccessLevel.PROTECTED)
public class OPlayer extends Storegable {

    private final UUID uuid;

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
