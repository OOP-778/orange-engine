package com.oop.orangeengine.database.util;

import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Tester {
    public static void t(String name, Runnable runnable) {
        Took took = Took.now();
        runnable.run();
        if (getEngine() == null)
            System.out.println(name + " took " + took.end() + "ms");
        else
            getEngine().getLogger().printDebug(name + " took " + took.end() + "ms");
    }

    public static <T> T twr(String name, Supplier<T> supplier) {
        Took took = Took.now();
        T obj = supplier.get();
        if (getEngine() == null)
            System.out.println(name + " took " + took.end() + "ms");
        else
            getEngine().getLogger().printDebug(name + " took " + took.end() + "ms");
        return obj;
    }
}
