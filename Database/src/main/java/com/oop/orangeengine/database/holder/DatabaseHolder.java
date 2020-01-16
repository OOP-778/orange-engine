package com.oop.orangeengine.database.holder;

import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;
import com.oop.orangeengine.database.object.DatabaseObject;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DatabaseHolder<T extends DatabaseObject, K> {

    Stream<T> dataStream();

    K genPrimaryKey(T object);

    default Set<T> loadedAllData() {
        return dataStream()
                .filter(object -> object.getObjectState() == DatabaseObject.ObjectState.LOADED)
                .collect(Collectors.toSet());
    }

    default Set<T> loadedAllDataBy(Predicate<T> filter) {
        return dataStream()
                .filter(object -> object.getObjectState() == DatabaseObject.ObjectState.LOADED && filter.test(object))
                .collect(Collectors.toSet());
    }

    default Optional<T> optionalLoadedDataBy(Predicate<T> filter) {
        return dataStream()
                .filter(object -> object.getObjectState() == DatabaseObject.ObjectState.LOADED && filter.test(object))
                .findFirst();
    }

    default T firstLoadedDataBy(Predicate<T> filter) {
        return dataStream()
                .filter(object -> object.getObjectState() == DatabaseObject.ObjectState.LOADED && filter.test(object))
                .findFirst()
                .orElse(null);
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

    DatabaseController dataController();
}
