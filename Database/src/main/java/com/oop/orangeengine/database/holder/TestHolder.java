package com.oop.orangeengine.database.holder;

import com.oop.orangeengine.database.object.DataController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class TestHolder implements DatabaseHolder<TestObject, UUID> {

    private ConcurrentHashMap<Integer, TestObject> objects = new ConcurrentHashMap<>();

    @Override
    public Stream<TestObject> dataStream() {
        return objects.values().stream();
    }

    @Override
    public UUID genPrimaryKey(TestObject object) {
        return UUID.randomUUID();
    }

    @Override
    public DatabaseController dataController() {
        return null;
    }
}
