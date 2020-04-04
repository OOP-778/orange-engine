package com.oop.orangeengine.database;

import com.google.common.collect.Maps;
import com.oop.orangeengine.database.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DatabaseController implements Saveable {

    private Map<Class<?>, DatabaseHolder<?, ?>> dataHolders = Maps.newConcurrentMap();

    @Getter @Setter
    private ODatabase database;

    public DatabaseController() {}

    public DatabaseController(ODatabase database) {
        this.database = database;
    }

    public <T extends DatabaseHolder<?, ?>> T holder(Class objectClass, Class<T> as) {
        return (T) holder(objectClass).orElse(null);
    }

    public <T extends DatabaseObject> Optional<DatabaseHolder<?, T>> holder(Class<T> objectClass) {
        return dataHolders.keySet().stream()
                .filter(clazz -> clazz.isAssignableFrom(objectClass) || objectClass.isAssignableFrom(clazz))
                .findFirst()
                .map(clazz -> (DatabaseHolder<?, T>) dataHolders.get(clazz));
    }

    public <T extends DatabaseObject> void registerHolder(Class<T> clazz, DatabaseHolder<?, T> holder) {
        Objects.requireNonNull(clazz.getAnnotation(Table.class), "Failed to register database holder for " + clazz.getSimpleName() + " because it doesn't have an table!");
        dataHolders.put(clazz, holder);
    }

    public void load() {
        dataHolders.values().forEach(this::load);
    }

    public void save() {
        dataHolders.values().forEach(DatabaseHolder::save);
    }
}
