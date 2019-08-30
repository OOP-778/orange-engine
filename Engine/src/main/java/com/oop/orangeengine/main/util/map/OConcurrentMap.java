package com.oop.orangeengine.main.util.map;

import com.oop.orangeengine.main.util.OptionalConsumer;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class OConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {

    public V putIfPresentUpdate(K key, V object, BiFunction<V, V, V> func) {

        AtomicReference<V> ref = new AtomicReference<>();
        getAsOptional(key).ifPresentOrElse(o2 -> ref.set(func.apply(object, o2)), () -> {

            ref.set(object);
            put(key, object);

        });

        return ref.get();

    }

    public OptionalConsumer<V> getAsOptional(K key) {
        return OptionalConsumer.of(Optional.ofNullable(get(key)));
    }

}
