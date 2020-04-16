package com.oop.orangeengine.hologram.wrapped.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.internal.Primitives;
import com.oop.orangeengine.main.util.OSimpleReflection;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class WrappedDataWatcher_1_8 extends WrappedDataWatcher {
    private static Constructor watchableObjectConst;

    private final Map<Byte, Object> items = Maps.newConcurrentMap();
    private static final Map<Class, Integer> classToId = new HashMap<>();

    static {
        try {
            classToId.put(String.class, 4);

            Class watchableObjectClass;
            watchableObjectClass = OSimpleReflection.findClass("{nms}.DataWatcher$WatchableObject");
            watchableObjectConst = watchableObjectClass.getConstructor(int.class, int.class, Object.class);

        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize DataWatcherWrapper for version 1.8", throwable);
        }
    }

    private WrappedDataWatcher_1_8() {}

    @Override
    public void addItem(byte id, Object value) {
        Integer typeId = classToId.get(Primitives.wrap(value.getClass()));
        Preconditions.checkArgument(typeId != null, "Failed to add item to DataWatcher because data type by " + value.getClass().getSimpleName() + " is not found!");

        try {
            Object item = watchableObjectConst.newInstance(typeId, id, value);
            items.remove(id);
            items.put(id, item);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to add item", throwable);
        }
    }

    @Override
    public List getList() {
        return Collections.singletonList(items.values());
    }
}
