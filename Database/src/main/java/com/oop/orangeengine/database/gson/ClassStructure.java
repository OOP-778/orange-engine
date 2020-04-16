package com.oop.orangeengine.database.gson;

import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.database.suppliers.FieldGatherer;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ClassStructure {

    private Class clazz;
    private Set<String> fieldNames = new HashSet<>();

    public ClassStructure(Class clazz) {
        this.clazz = clazz;
        FieldGatherer
                .create()
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                .filter(field -> field.getAnnotation(SerializedName.class) != null)
                .gather(clazz)
                .stream()
                .map(field -> field.getAnnotation(SerializedName.class))
                .map(SerializedName::value)
                .forEach(fieldNames::add);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Collection) {
            for (String name : (Collection<String>) o) {
                if (!fieldNames.contains(name)) return false;
            }
            return true;
        }
        return super.equals(o);
    }
}
