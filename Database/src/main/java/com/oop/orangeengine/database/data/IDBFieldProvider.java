package com.oop.orangeengine.database.data;

public interface IDBFieldProvider<F, S> {

    F provide(S s);

}
