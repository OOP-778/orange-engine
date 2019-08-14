package com.oop.orangeengine.main;

public class ClassLoader {

    public static void load() {
        try {

            //Load EventsSubscription
            if (classFound("com.oop.orangeengine.eventsSubscription.SubscriptionFactory"))
                Class.forName("com.oop.orangeengine.eventsSubscription.SubscriptionFactory").newInstance();

            //Load Reflection
            if (classFound("com.oop.orangeengine.reflection.OReflection"))
                Class.forName("com.oop.orangeengine.reflection.OReflection").newInstance();

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
