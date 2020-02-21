package com.oop.orangeengine.database;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.oop.orangeengine.database.newversion.DatabaseController;
import com.oop.orangeengine.database.newversion.gson.MapFactory;
import com.oop.orangeengine.database.newversion.gson.RuntimeClassFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.util.Map;
import java.util.function.Supplier;

public class Tester {

    static interface TestingInt {

    }

    static class TestObjMap implements TestingInt {
       public Map<TEstEnum, Testing.TestObject> map = Maps.newConcurrentMap();
        public String v = "wa'gawgawgawg";

        public TestObjMap() {
            map.put(TEstEnum.WGAWGAWg, new Testing.TestObject());
            map.put(TEstEnum.SAWGW, new Testing.TestObject());
        }
    }

    public static void main(String[] args) {
        try {

            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(RuntimeClassFactory.of(Object.class))
                    .registerTypeAdapterFactory(new MapFactory())
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();

            String serialized = gson.toJson(new TestObjMap());
            System.out.println(serialized);

            System.out.println(gson.fromJson(serialized, TestingInt.class));
        } catch (Throwable thrw) {
            thrw.printStackTrace();
            throw new IllegalStateException(thrw);
        }
    }

    public static void t(String action, Tookable tookable) {
        tookable.runWithTook(action);
    }

    public static <T> T twr(String action, Supplier<T> supplier) {
        long then = System.currentTimeMillis();
        T obj = supplier.get();
        long current = System.currentTimeMillis();
        System.out.println(action + " took " + (current - then) + "ms");
        return obj;
    }

    public static class DabController extends DatabaseController {
        public DabController(ODatabase database) {
            setDatabase(database);
        }
    }

    public static interface Tookable extends Runnable {
        default void runWithTook(String actionName) {
            long then = System.currentTimeMillis();
            run();
            long current = System.currentTimeMillis();
            System.out.println(actionName + " took " + (current - then) + "ms");
        }
    }

    public static enum TEstEnum {
        SAWGW,
        WGAWGAWg
    }

}
