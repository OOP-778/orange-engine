package com.oop.orangeEngine.main.util;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor(staticName = "of")
public class OSwapper<T> {

    private final T object;
    private boolean accepted = false;

    public OSwapper<T> swap(Predicate<T> filter, Consumer<T> consumer) {
        if (accepted) return this;

        if (filter.test(object)) {

            accepted = true;
            consumer.accept(object);

        }

        return this;

    }


}
