package com.oop.orangeengine.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@EqualsAndHashCode
public class DatabaseField<T> {
    private T object = null;

    @Setter
    @Getter
    private boolean requiresUpdate = false;

    @Getter
    private Class<T> clazz;

    public DatabaseField(@NonNull T object) {
        this.object = object;
        this.clazz = (Class<T>) object.getClass();
    }

    public DatabaseField(Class<T> clazz) {
        this.clazz = clazz;
    }

    public boolean isPresent() {
        return object != null;
    }

    public void set(T object) {
        if (this.object == object) return;

        this.object = object;
        this.requiresUpdate = true;
    }

    public void set(T object, boolean update) {
        if (this.object == object) return;

        this.object = object;
        if (update)
            this.requiresUpdate = update;
    }


    public T get() {
        return object;
    }

    public <O extends T> O getAs(Class<O> clazz) {
        return (O) get();
    }
}
