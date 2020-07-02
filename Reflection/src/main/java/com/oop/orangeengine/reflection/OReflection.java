package com.oop.orangeengine.reflection;

import com.oop.orangeengine.main.Engine;

import java.util.HashMap;
import java.util.Map;

public class OReflection {

    private Map<String, OClass> classMap = new HashMap<>();
    private static OReflection INSTANCE;

    static {
        INSTANCE = new OReflection();
    }

    public OClass resolveClass(String path) {
        try {

            OClass oClass = classMap.get(path);
            if (oClass == null) {

                Class<?> klass = Class.forName(path);
                oClass = new OClass(klass);
                oClass.setExceptionHandler(error -> Engine.getInstance().getLogger().error(error));
                classMap.put(path, oClass);

            }

            return oClass;

        } catch (Exception ex) {
            Engine.getInstance().getLogger().error(ex);
        }

        return null;

    }

    public static OReflection getInstance() {
        return INSTANCE;
    }
}
