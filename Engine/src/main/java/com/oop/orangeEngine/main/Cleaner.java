package com.oop.orangeEngine.main;

import com.oop.orangeEngine.main.component.AEngineComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Cleaner extends AEngineComponent {

    private HashMap<Class<?>, Set<Field>> toClean = new HashMap<>();

    Cleaner() {
        super();

        //Init clean method onDisable
        Engine.getInstance().getOwning().onDisable(this::clean);

    }


    public void registerClass(Class<?> clazz) {

        Set<Field> fields = toClean.get(clazz);
        if(fields == null) {

            fields = new HashSet<>();
            toClean.put(clazz, fields);

        }

        //Loop through fields
        for(Field field : clazz.getFields()) {
            if(Modifier.isStatic(field.getModifiers())) {

                field.setAccessible(true);
                fields.add(field);

            }
        }

    }

    private void clean() {

        toClean.values().forEach(fields -> fields.forEach(field -> {

            try {
                //EZ Static Abuse Fix
                field.set(null, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }));

    }


    @Override
    public String getName() {
        return "Cleaner";
    }
}
