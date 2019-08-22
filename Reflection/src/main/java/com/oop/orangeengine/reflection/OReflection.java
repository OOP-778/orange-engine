package com.oop.orangeengine.reflection;

import com.oop.orangeengine.main.Cleaner;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.util.DefaultInitialization;

import java.util.HashMap;
import java.util.Map;

public class OReflection  {

    private Map<String, OClass> classMap = new HashMap<>();
    private static OReflection INSTANCE;

    @DefaultInitialization
    public OReflection() {

        if (INSTANCE != null) {

            Engine.getInstance().getLogger().error(new IllegalAccessException("Instance of OReflection class already exists!"));
            return;

        }

        INSTANCE = this;
        Engine.getInstance().findComponentByClass(Cleaner.class).registerClass(OReflection.class);

    }

    public OClass resolveClass(String path) {

        try {

            OClass oClass = classMap.get(path);
            if(oClass == null) {

                Class<?> klass = Class.forName(path);
                oClass = new OClass(klass);
                oClass.setExceptionHandler(error -> Engine.getInstance().getLogger().error(error));
                classMap.put(path, oClass);

            }

            return oClass;

        } catch (Exception ex){
            Engine.getInstance().getLogger().error(ex);
        }

        return null;

    }

    public static OReflection getInstance() {
        return INSTANCE;
    }
}
