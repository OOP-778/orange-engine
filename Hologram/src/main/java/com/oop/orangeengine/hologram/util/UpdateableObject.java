package com.oop.orangeengine.hologram.util;

import lombok.Getter;

public class UpdateableObject<T> {

    private T object;

    @Getter
    private boolean updated;

    public UpdateableObject(T object) {
        this.object = object;
    }

    public T get() {
        updated = false;
        return object;
    }

    public T current() {
        return object;
    }

    public void set(T object) {
        this.object = object;
        updated = true;
    }
}
