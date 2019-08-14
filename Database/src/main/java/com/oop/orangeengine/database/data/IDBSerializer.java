package com.oop.orangeengine.database.data;

public interface IDBSerializer<T> {

    String serialize(T object);


}
