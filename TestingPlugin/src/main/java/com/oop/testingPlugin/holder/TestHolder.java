package com.oop.testingPlugin.holder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.newversion.DatabaseController;
import com.oop.orangeengine.database.newversion.DatabaseHolder;
import com.oop.orangeengine.database.newversion.DatabaseObject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class TestHolder implements DatabaseHolder<UUID, TestObject> {

    private DatabaseController databaseController;
    public TestHolder(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    private Map<UUID, TestObject> data = Maps.newConcurrentMap();

    @Override
    public Stream<TestObject> dataStream() {
        return data.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(TestObject object) {
        return UUID.randomUUID();
    }

    @Override
    public void pushObject(TestObject obj, boolean isNew) {
        System.out.println("Pushing " + obj);
        data.put(obj.getUuid(), obj);
    }

    @Override
    public void deleteObject(TestObject obj) {

    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(TestObject.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return databaseController;
    }
}
