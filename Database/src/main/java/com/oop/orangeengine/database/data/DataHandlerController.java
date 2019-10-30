package com.oop.orangeengine.database.data;

import com.oop.orangeengine.main.Engine;
import org.nustaq.serialization.FSTConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataHandlerController {

    private static FSTConfiguration fstConfiguration = FSTConfiguration.createJsonConfiguration();
    private static DataHandlerController INSTANCE = new DataHandlerController();

    private Map<Class, IDataHandler> dataHandlerMap = new HashMap<>();
    private final IDataHandler<Object> defaultHandler = new IDataHandler<Object>() {
        @Override
        public Object load(String serialized) {
            return fstConfiguration.asObject(serialized.getBytes());
        }

        @Override
        public String serialize(Object object) {
            return fstConfiguration.asJsonString(object);
        }
    };

    private DataHandlerController() {
        dataHandlerMap.put(UUID.class, new IDataHandler<UUID>() {
            @Override
            public UUID load(String serialized) {
                return UUID.fromString(serialized);
            }

            @Override
            public String serialize(UUID object) {
                return object.toString();
            }
        });
        dataHandlerMap.put(Boolean.class, new IDataHandler() {
            @Override
            public Object load(String serialized) {
                return Boolean.parseBoolean(serialized);
            }

            @Override
            public String serialize(Object object) {
                return object.toString();
            }
        });
    }

    public static DataHandlerController getInstance() {
        return INSTANCE;
    }

    public <T> IDataHandler<T> findDataHandler(Class<T> klass) {
        Class correctKey = dataHandlerMap.keySet().stream()
                .filter(klass2 -> klass2.isAssignableFrom(klass))
                .findFirst()
                .orElse(null);

        if(correctKey == null)
            return (IDataHandler<T>) defaultHandler;

        IDataHandler<T> handler = dataHandlerMap.get(correctKey);
        if (handler == null)
            return (IDataHandler<T>) defaultHandler;

        return handler;
    }

    public <T> void registerDataHandler(Class<T> klass, IDataHandler<T> dataHandler) {
        dataHandlerMap.put(klass, dataHandler);
    }

}
