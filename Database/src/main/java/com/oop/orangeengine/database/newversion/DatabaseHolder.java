package com.oop.orangeengine.database.newversion;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DatabaseHolder<K, T extends DatabaseObject> extends Saveable {
    Stream<T> dataStream();

    K generatePrimaryKey(T object);

    void pushObject(T obj, boolean isNew);

    default void pushObject(T obj) {
        pushObject(obj, true);
    }

    void deleteObject(T obj);

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

    DatabaseController getDatabaseController();

    default void onSave(T object) {}

    default void onLoad(T object) {}
}
