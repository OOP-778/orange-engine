package com.oop.orangeengine.hologram.wrapped.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.OSimpleReflection;

import java.lang.reflect.*;
import java.util.*;

public class WrappedDataWatcher_1_9 extends WrappedDataWatcher {

    private static Constructor
            dataWatcherItemConst;

    private static Method idToObject;

    private Map<Byte, Object> items = Maps.newConcurrentMap();
    private static Map<String, Object> fieldToSerializer = Maps.newHashMap();
    private static Map<String, String> dataWatchers = Maps.newHashMap();

    private WrappedDataWatcher_1_9() {}

    static {
        try {
            Class
                    dataWatcherObjectClass,
                    dataWatcherItemClass,
                    dataWatcherRegistryClass,
                    dataWatcherSerializerClass;

            dataWatcherItemClass = OSimpleReflection.findClass("{nms}.DataWatcher$Item");
            dataWatcherObjectClass = OSimpleReflection.findClass("{nms}.DataWatcherObject");
            dataWatcherRegistryClass = OSimpleReflection.findClass("{nms}.DataWatcherRegistry");
            dataWatcherSerializerClass = OSimpleReflection.findClass("{nms}.DataWatcherSerializer");

            for (Field field : dataWatcherRegistryClass.getFields()) {
                fieldToSerializer.put(field.getName(), field.get(null));

                Class<?> genericType = getGenericType(field);
                if (genericType != null) {
                    dataWatchers.put(genericType.getSimpleName(), field.getName());
                }

                dataWatchers.put("String", "d");
                dataWatchers.put("IChatBaseComponent", "e");
                dataWatchers.put("ChatComponentText", "e");
            }

            idToObject = dataWatcherSerializerClass.getMethod("a", int.class);
            dataWatcherItemConst = dataWatcherItemClass.getConstructor(dataWatcherObjectClass, Object.class);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize DataWatcher for version 1.9-1.12.2");
        }
    }

    private static Optional<Object> getSerializer(Object val) {
        Object serializer = fieldToSerializer.get(dataWatchers.get(val.getClass().getSimpleName()));
        return Optional.ofNullable(serializer);
    }

    public Object getObject(Object value, byte id) {
        if (value instanceof Optional)
            value = ((Optional) value).orElse(null);

        return getSerializer(Objects.requireNonNull(value, "Failed to get DataWatcherObject cause value is null!")).map(serializer -> {
            try {
                return idToObject.invoke(id);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static Class<?> getGenericType(Field field) {
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>)
            return (Class<?>) type;

        else
            return null;
    }

    @Override
    public void addItem(byte id, Object value) {
        try {
            Object dataWatcherObject = getObject(value, id);
            Object item = dataWatcherItemConst.newInstance(dataWatcherObject, value);
            items.remove(id);
            items.put(id, item);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to add item", throwable);
        }
    }

    @Override
    public List<Object> getList() {
        return Collections.singletonList(items.values());
    }
}
