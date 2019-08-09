package com.oop.orangeEngine.main;

public class ClassLoader {

    public static void load() {
        try {

            //Load EventsSubscription
            if (classFound("com.oop.orangeEngine.eventsSubscription.SubscriptionFactory"))
                Class.forName("com.oop.orangeEngine.eventsSubscription.SubscriptionFactory").newInstance();

            //Load Reflection
            if (classFound("com.oop.orangeEngine.reflection.OReflection"))
                Class.forName("com.oop.orangeEngine.reflection.OReflection").newInstance();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static boolean classFound(String path) {
        try {
            Class.forName(path);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
