package com.oop.orangeengine.database.testing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.database.gson.ClassRegistry;
import com.oop.orangeengine.database.gson.CollectionFactory;
import com.oop.orangeengine.database.gson.MapFactory;
import com.oop.orangeengine.database.gson.ObjectFactory;
import com.oop.orangeengine.database.util.Tester;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Testing {
    public static void main(String[] args) throws ClassNotFoundException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.schedule(() -> {
            try {
                Gson gson = new GsonBuilder()
                        .serializeNulls()
                        .registerTypeAdapterFactory(ObjectFactory.FACTORY)
                        .registerTypeAdapterFactory(new MapFactory())
                        .registerTypeAdapterFactory(new CollectionFactory())
                        .setPrettyPrinting()
                        .create();

                ClassRegistry.registerHierarchy(TestingObject.class);

                Set<TestingObject> testingObjects = new HashSet<>();
                for (int i = 0; i < 1; i++) {
                    testingObjects.add(new TestingObject());
                }

                Set<TestingObject> finalTestingObjects = testingObjects;
                String serialized = Tester.twr("toJson (" + testingObjects.size() + " objects)", () -> gson.toJson(finalTestingObjects));
                System.out.println(serialized);

                testingObjects = (Set<TestingObject>) Tester.twr("fromJson", () -> gson.fromJson(serialized, Set.class));
                for (TestingObject object : testingObjects) {
                    System.out.println(object);
                }
                System.out.println(gson.toJson(TestEnum.HELLO));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }, 2, TimeUnit.SECONDS);
    }
}
