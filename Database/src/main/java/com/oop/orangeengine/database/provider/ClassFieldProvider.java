package com.oop.orangeengine.database.provider;

import com.oop.orangeengine.main.util.pair.OPair;

import java.util.HashSet;
import java.util.Set;

public class ClassFieldProvider<T> {

    private Set<OPair<String, IDBFieldProvider<T>>> fieldProviders = new HashSet<>();

    public ClassFieldProvider<T> newProvider(String fieldName, IDBFieldProvider<T> provider) {
        fieldProviders.add(new OPair<>(fieldName, provider));
        return this;
    }

}
