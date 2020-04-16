package com.oop.orangeengine.hologram.wrapped.impl;

import com.oop.orangeengine.main.util.version.OVersion;
import lombok.SneakyThrows;

import java.util.List;

public abstract class WrappedDataWatcher {
    private static Class<? extends WrappedDataWatcher> implementationClass;

    static {
        if (OVersion.is(8))
            implementationClass = WrappedDataWatcher_1_8.class;

        else if (OVersion.isBefore(13))
            implementationClass = WrappedDataWatcher_1_9.class;
    }

    @SneakyThrows
    public static WrappedDataWatcher construct() {
        return implementationClass.newInstance();
    }

    public abstract void addItem(byte id, Object value);

    public abstract List getList();
}
