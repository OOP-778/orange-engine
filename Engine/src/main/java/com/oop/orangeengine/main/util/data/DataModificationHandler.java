package com.oop.orangeengine.main.util.data;

public interface DataModificationHandler<T> {
    void onAdd(T t);

    void onRemove(T t);
}
