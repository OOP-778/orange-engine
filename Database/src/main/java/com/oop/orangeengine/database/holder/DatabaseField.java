package com.oop.orangeengine.database.holder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class DatabaseField<T> {
    private T object = null;

    @Setter
    private boolean requiresUpdate = false;

    private int hashCode = 0;
    private Class<T> clazz;

    public DatabaseField(@NonNull T object) {
        this.object = object;
        this.hashCode = object.hashCode();
        this.clazz = (Class<T>) object.getClass();
    }

    public DatabaseField(Class<T> clazz) {
        this.clazz = clazz;
    }

    public boolean isPresent() {
        return object != null;
    }

    public void set(T object) {
        this.object = object;
        this.requiresUpdate = true;
    }

    public void set(T object, boolean update) {
        this.object = object;
        if (update)
            this.requiresUpdate = update;
    }
}
