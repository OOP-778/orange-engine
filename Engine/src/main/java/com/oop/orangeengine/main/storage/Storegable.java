package com.oop.orangeengine.main.storage;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.OptionalConsumer;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Getter
public abstract class Storegable {

    private Map<String, Object> data = Maps.newConcurrentMap();

    public boolean containsData(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void store(String key, Object object) {
        data.remove(key);
        data.put(key, object);
    }

    public <T> T storeIfNotPresent(String key, T object) {

        Optional<T> optional = Optional.ofNullable((T) data.get(key));
        if (optional.isPresent())
            return optional.get();

        else {
            data.put(key, object);
            return object;
        }
    }

    public <T> T storeIfPresentUpdate(String key, T object, BiFunction<T, T, T> func) {

        AtomicReference<T> ref = new AtomicReference<>();
        grab(key).ifPresentOrElse(o2 -> ref.set(func.apply(object, (T) o2)), () -> {

            ref.set(object);
            data.put(key, object);

        });

        return ref.get();
    }

    public <T> T storeIfPresentReplace(String key, T object) {
        data.remove(key);
        data.put(key, object);
        return object;
    }

    public <T> OptionalConsumer<T> grab(String key) {
        return OptionalConsumer.of(Optional.ofNullable((T) data.get(key)));
    }

    public <T> OptionalConsumer<T> grab(String key, Class<T> type) {
        return grab(key);
    }

    public void forEach(BiConsumer<String, Object> action) {
        data.forEach(action);
    }

    public void newMap() {
        this.data = Maps.newConcurrentMap();
    }

}
