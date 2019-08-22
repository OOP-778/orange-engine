package com.oop.orangeengine.database.provider;

import java.util.HashMap;
import java.util.Map;

public class FieldProviderController {

    private static final FieldProviderController INSTANCE = new FieldProviderController();
    private Map<Class, ClassFieldProvider> classFieldProviderMap = new HashMap<>();

    private FieldProviderController() {
    }

    public static FieldProviderController getInstance() {
        return INSTANCE;
    }

    public void registerProvider(Class klass, ClassFieldProvider provider) {
        classFieldProviderMap.put(klass, provider);
    }

    public ClassFieldProvider findProvider(Class klass) {
        return classFieldProviderMap.get(klass);
    }
}
