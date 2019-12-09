package com.oop.orangeengine.main.util;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalConsumer<T> implements Consumer<Optional<T>> {
    private Optional<T> optional;

    private OptionalConsumer(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> OptionalConsumer<T> of(Optional<T> optional) {
        return new OptionalConsumer<>(optional);
    }

    public static <T> OptionalConsumer<T> of(T nullable) {
        return new OptionalConsumer<>(Optional.ofNullable(nullable));
    }

    public OptionalConsumer<T> ifPresent(Consumer<T> c) {
        optional.ifPresent(c);
        return this;
    }

    public void ifPresentOrElse(Consumer<T> ifPresent, Runnable orElse) {
        if(optional.isPresent())
            ifPresent.accept(optional.get());

        else
            orElse.run();

    }

    public OptionalConsumer<T> ifNotPresent(Runnable r) {
        if (!optional.isPresent()) {
            r.run();
        }
        return this;
    }

    public T get() {
        return optional.orElse(null);
    }

    public <T> T get(Class<T> type) {
        return (T) optional.orElse(null);
    }

    @Override
    public void accept(Optional<T> t) {
        optional = t;
    }

    public boolean isPresent() {
        return optional.isPresent();
    }
}