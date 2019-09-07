package com.oop.orangeengine.main.util.data.map;

import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.DataModificationHandler;
import lombok.Setter;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class OMap<K, V> extends HashMap<K, V> {
    @Setter
    private DataModificationHandler<V> handler;

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

    @Override
    public V remove(Object o) {
        V value = get(o);
        if (handler != null)
            handler.onRemove(value);

        return super.remove(o);
    }

    @Override
    public V put(K k, V v) {
        if (handler != null)
            handler.onAdd(v);

        return super.put(k, v);
    }
}
