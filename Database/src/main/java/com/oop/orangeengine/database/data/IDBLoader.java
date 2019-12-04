package com.oop.orangeengine.database.data;

public interface IDBLoader<T> {

    T load(String serialized, Class<T> type);



}
