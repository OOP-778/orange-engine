package com.oop.orangeengine.main.storage;

import com.oop.orangeengine.main.util.OptionalConsumer;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public abstract class Storegable extends HashMap<String, Object> {

    public boolean containsData(String key) {
        return containsKey(key);
    }

    public void store(String key, Object object) {

        remove(key);
        put(key, object);

    }

    public <T> T storeIfNotPresent(String key, T object) {

        Optional<T> optional = Optional.ofNullable((T) get(key));
        if (optional.isPresent())
            return optional.get();

        else {
            put(key, object);
            return object;
        }

    }

    public <T> T storeIfPresentUpdate(String key, T object, BiFunction<T, T, T> func) {

        AtomicReference<T> ref = new AtomicReference<>();
        grab(key).ifPresentOrElse(o2 -> ref.set(func.apply(object, (T) o2)), () -> {

            ref.set(object);
            put(key, object);

        });

        return ref.get();

    }

    public <T> OptionalConsumer<T> grab(String key) {
        return OptionalConsumer.of(Optional.ofNullable((T) get(key)));
    }

    public <T> OptionalConsumer<T> grab(String key, Class<T> type) {
        return grab(key);
    }

}
