package com.oop.orangeengine.main.util.data.set;

import com.oop.orangeengine.main.util.data.DataModificationHandler;
import lombok.Setter;

import java.util.HashSet;

public class OSet<T> extends HashSet<T> {
    @Setter
    private DataModificationHandler<T> handler;

    @Override
    public boolean add(T t) {
        if (handler != null)
            handler.onAdd(t);

        return super.add(t);
    }

    @Override
    public boolean remove(Object o) {
        if (handler != null)
            handler.onRemove((T) o);

        return super.remove(o);
    }
}
