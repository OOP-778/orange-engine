package com.oop.orangeengine.database;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.newversion.DatabaseController;
import com.oop.orangeengine.database.newversion.DatabaseHolder;
import com.oop.orangeengine.database.newversion.DatabaseObject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class TestDBHolder implements DatabaseHolder<UUID, TestObject> {

    private Map<UUID, TestObject> objs = Maps.newConcurrentMap();
    private DatabaseController dc;

    public TestDBHolder(DatabaseController dc) {
        this.dc = dc;
        dc.registerHolder(TestObject.class, this);
    }

    @Override
    public Stream<TestObject> dataStream() {
        return objs.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(TestObject object) {
        return UUID.randomUUID();
    }

    @Override
    public void pushObject(TestObject obj, boolean isNew) {
        objs.put(obj.getUuid(), obj);
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
        return dc;
    }
}
