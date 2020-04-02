package com.oop.orangeengine.database.testing;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseController;
import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.DatabaseObject;
import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class TestingHolder implements DatabaseHolder<UUID, TestingObject> {

    private Map<UUID, TestingObject> map = Maps.newConcurrentMap();
    private com.oop.orangeengine.database.testing.DatabaseController databaseController;

    public TestingHolder(com.oop.orangeengine.database.testing.DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    @Override
    public Stream<TestingObject> dataStream() {
        return map.values().stream();
    }

    @Override
    public UUID generatePrimaryKey(TestingObject object) {
        return object.getUuid();
    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(TestingObject.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    @Override
    public void onAdd(TestingObject object, boolean isNew) {
        map.put(object.getUuid(), object);
    }

    @Override
    public void onRemove(TestingObject object) {
        map.remove(object.getUuid());
    }
}
