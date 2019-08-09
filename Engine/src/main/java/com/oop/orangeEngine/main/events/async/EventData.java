package com.oop.orangeEngine.main.events.async;

import com.oop.orangeEngine.main.storage.Storegable;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EventData extends Storegable {

    private static Map<Class<? extends Event>, BiConsumer<? extends Event, EventData>> dataTypes = new HashMap<>();

    public static <T extends Event> void registerType(Class<T> classz, BiConsumer<T, EventData> biConsumer) {
        dataTypes.put(classz, biConsumer);
    }

    public static <T extends Event> BiConsumer<T, EventData> getDataType(Class<T> type) {

        return (BiConsumer<T, EventData>) getDataTypes().get(type);

    }

    public static Map<Class<? extends Event>, BiConsumer<? extends Event, EventData>> getDataTypes() {
        return dataTypes;
    }

}
