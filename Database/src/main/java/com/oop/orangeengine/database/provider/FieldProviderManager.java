package com.oop.orangeengine.database.provider;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FieldProviderManager {

    private static final FieldProviderManager INSTANCE = new FieldProviderManager();
    private Map<Class, ClassFieldProvider> classFieldProviderMap = new HashMap<>();

    private FieldProviderManager() {}

    public static FieldProviderManager getInstance() {
        return INSTANCE;
    }

    public void registerProvider(Class klass, ClassFieldProvider provider) {
        classFieldProviderMap.put(klass, provider);
    }
}
