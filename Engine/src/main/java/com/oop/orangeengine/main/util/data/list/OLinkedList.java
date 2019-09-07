package com.oop.orangeengine.main.util.data.list;

import com.oop.orangeengine.main.util.data.DataModificationHandler;
import lombok.Setter;

import java.util.LinkedList;

public class OLinkedList<T> extends LinkedList<T> {
    @Setter
    private DataModificationHandler<T> handler;

    @Override
    public boolean add(T t) {
        if(handler != null)
            handler.onAdd(t);

        return super.add(t);
    }

    @Override
    public boolean remove(Object o) {
        if(handler != null)
            handler.onRemove((T) o);

        return super.remove(o);
    }
}
