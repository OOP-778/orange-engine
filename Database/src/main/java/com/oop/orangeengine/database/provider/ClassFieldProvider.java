package com.oop.orangeengine.database.provider;

import com.oop.orangeengine.main.util.pair.OPair;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFieldProvider<T> {

    private Set<OPair<String, IDBFieldProvider<T>>> fieldProviders = ConcurrentHashMap.newKeySet();

    public ClassFieldProvider<T> newProvider(String fieldName, IDBFieldProvider<T> provider) {
        fieldProviders.add(new OPair<>(fieldName, provider));
        return this;
    }

    public IDBFieldProvider<T> findProvider(String fieldName) {
        OPair<String, IDBFieldProvider<T>> prov = fieldProviders.stream()
                .filter(provider -> provider.getFirst().equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(null);

        if (prov == null)
            return null;

        else
            return prov.getSecond();
    }

}
