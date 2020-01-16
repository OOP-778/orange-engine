package com.oop.orangeengine.database.holder;

import com.avaje.ebeaninternal.server.cluster.DataHolder;
import com.google.common.collect.Maps;
import com.oop.orangeengine.database.ODatabase;
import lombok.Getter;

import java.util.Map;

public abstract class DatabaseController {

    private Map<Class, DatabaseHolder> dataHolders = Maps.newConcurrentMap();

    @Getter
    private ODatabase database;

    void setDatabase(ODatabase database) {
        this.database = database;
    }

    public <T extends DataHolder> T holder(Class<T> clazz) {
        return (T) dataHolders.get(clazz);
    }
}
