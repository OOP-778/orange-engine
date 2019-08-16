package com.oop.orangeengine.database.data;

import java.util.HashMap;
import java.util.Map;

public class DataHandlerManager {

    private static DataHandlerManager INSTANCE = new DataHandlerManager();

    private Map<Class, IDataHandler> dataHandlerMap = new HashMap<>();

    private DataHandlerManager() {}

    public static DataHandlerManager getInstance() {
        return INSTANCE;
    }

    public <T> IDataHandler<T> findDataHandler(Class<T> klass) {
        return dataHandlerMap.get(klass);
    }

}
