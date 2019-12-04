package com.oop.orangeengine.database.data;

import com.google.gson.Gson;
import com.oop.orangeengine.item.ItemStackUtil;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.oop.orangeengine.main.Engine.getEngine;

public class DataHandlerController {

    private static Gson gson;
    private static DataHandlerController INSTANCE = new DataHandlerController();

    private Map<Class, IDataHandler> dataHandlerMap = new HashMap<>();
    private final IDataHandler<Object> defaultHandler = new IDataHandler<Object>() {
        @Override
        public Object load(String serialized, Class type) {
            return gson.fromJson(serialized, type);
        }

        @Override
        public String serialize(Object object) {
            return gson.toJson(object);
        }
    };

    private DataHandlerController() {
        gson = getEngine().getGson();
        dataHandlerMap.put(UUID.class, new IDataHandler<UUID>() {
            @Override
            public UUID load(String serialized, Class type) {
                return UUID.fromString(serialized);
            }

            @Override
            public String serialize(UUID object) {
                return object.toString();
            }
        });
        dataHandlerMap.put(Boolean.class, new IDataHandler() {
            @Override
            public Object load(String serialized, Class type) {
                return Boolean.parseBoolean(serialized);
            }

            @Override
            public String serialize(Object object) {
                return object.toString();
            }
        });

        try {
            dataHandlerMap.put(ItemStack.class, new IDataHandler() {
                @Override
                public Object load(String serialized, Class type) {
                    return ItemStackUtil.itemStackFromJson(serialized);
                }

                @Override
                public String serialize(Object object) {
                    try {
                        return ItemStackUtil.itemStackToJson((ItemStack) object);
                    } catch (Exception e) {
                        getEngine().getLogger().error(e);
                    }
                    return null;
                }
            });

        } catch (Exception ignored) {
        }
    }

    public static DataHandlerController getInstance() {
        return INSTANCE;
    }

    public <T> IDataHandler<T> findDataHandler(Class<T> klass) {
        Class correctKey = dataHandlerMap.keySet().stream()
                .filter(klass2 -> klass2.isAssignableFrom(klass))
                .findFirst()
                .orElse(null);

        if (correctKey == null)
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
