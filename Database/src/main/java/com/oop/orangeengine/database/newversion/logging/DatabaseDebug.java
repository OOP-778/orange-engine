package com.oop.orangeengine.database.newversion.logging;

import static com.oop.orangeengine.main.Engine.getEngine;

public class DatabaseDebug {

    public static void debugWithTook(String action, Took took) {
        took.runWithTook(action);
    }

    public static interface Took extends Runnable {
        default void runWithTook(String actionName) {
            long then = System.currentTimeMillis();
            run();
            long current = System.currentTimeMillis();

            if (getEngine() != null)
                getEngine().getLogger().printDebug(actionName + " took " + (current - then) + "ms");

            else
                System.out.println(actionName + " took " + (current - then) + "ms");
        }
    }

}
