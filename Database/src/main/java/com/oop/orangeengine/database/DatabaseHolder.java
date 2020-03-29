package com.oop.orangeengine.database;

import com.oop.orangeengine.database.util.ObjectState;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DatabaseHolder<K, T extends DatabaseObject> extends Saveable {
    Stream<T> dataStream();

    K generatePrimaryKey(T object);

    Set<Class<? extends DatabaseObject>> getObjectVariants();

    default Set<T> loadedData() {
        return dataStream()
                .filter(object -> object.getObjectState() == ObjectState.LOADED)
                .collect(Collectors.toSet());
    }

    default Set<T> loadedDataBy(Predicate<T> filter) {
        return dataStream()
                .filter(object -> object.getObjectState() == ObjectState.LOADED && filter.test(object))
                .collect(Collectors.toSet());
    }

    default Set<T> data() {
        return dataStream().collect(Collectors.toSet());
    }

    default Set<T> dataBy(Predicate<T> filter) {
        return dataStream()
                .filter(filter)
                .collect(Collectors.toSet());
    }

    default T firstDataBy(Predicate<T> filter) {
        return dataStream()
                .filter(filter)
                .findFirst()
                .orElse(null);
    }

    default void remove(T object) {
        remove(Collections.singletonList(object), getDatabaseController());
        onRemove(object);
    }

    default void add(T object, boolean isNew) {
        object.setHolder(this);
        onAdd(object, isNew);
        if (isNew)
            save(object, this);

    }

    default void add(T object) {
        add(object, true);
    }

    default boolean contains(T object) {
        return dataStream().anyMatch(obj2 -> obj2.equals(object));
    }

    DatabaseController getDatabaseController();

    default void onSave(T object) {}

    void onAdd(T object, boolean isNew);

    void onRemove(T object);
}
