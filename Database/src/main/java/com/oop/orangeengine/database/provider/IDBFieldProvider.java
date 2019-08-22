package com.oop.orangeengine.database.provider;

public interface IDBFieldProvider<O> {

    Object provide(O owner);

}
