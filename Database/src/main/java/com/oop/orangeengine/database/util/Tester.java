package com.oop.orangeengine.database.util;

import java.util.function.Supplier;

public class Tester {
    public static void t(String name, Runnable runnable) {
        Took took = Took.now();
        runnable.run();
        System.out.println(name + " took " + took.end() + "ms");
    }

    public static <T> T twr(String name, Supplier<T> supplier) {
        Took took = Took.now();
        T obj = supplier.get();
        System.out.println(name + " took " + took.end() + "ms");
        return obj;
    }
}
