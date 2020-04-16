package com.oop.orangeengine.database.gson;

import com.google.gson.internal.Primitives;
import com.oop.orangeengine.database.suppliers.FieldGatherer;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassHierarchyGatherer {
    private Class clazz;

    private ClassHierarchyGatherer(Class clazz) {
        this.clazz = clazz;
    }

    public static ClassHierarchyGatherer gatherer(Class<? extends Object> clazz) {
        return new ClassHierarchyGatherer(clazz);
    }

    public Set<Class> gather() {
        final Set<Class> hierarchyClasses = new HashSet<>();
        _gather(hierarchyClasses, clazz);
        return hierarchyClasses;
    }

    private void _gather(Set<Class> classes, Class childClass) {
        if (childClass.isEnum()) return;

        for (Field field : Collections.unmodifiableCollection(FieldGatherer.create().filter(field -> !Modifier.isTransient(field.getModifiers())).gather(childClass))) {
            Set<Class> classes1 = processField(field);
            classes.addAll(classes1);

            for (Class aClass : classes1)
                _gather(classes, aClass);
        }
    }

    private void processGenericField(Set<Class> classes, ParameterizedType type) {
        for (Type paramType : type.getActualTypeArguments()) {
            if (paramType instanceof ParameterizedType) {
                processGenericField(classes, (ParameterizedType) paramType);
                continue;
            }

            Class clazz = (Class) paramType;
            if (!shouldAdd(clazz)) continue;

            classes.add(clazz);
        }
    }

    @SneakyThrows
    private Set<Class> processField(Field field) {
        Set<Class> classes = new HashSet<>();
        if (field.getGenericType() instanceof ParameterizedType)
            processGenericField(classes, (ParameterizedType) field.getGenericType());

        if (shouldAdd(field.getType()))
            classes.add(field.getType());

        return classes;
    }

    private boolean shouldAdd(Class clazz) {
        clazz = Primitives.wrap(clazz);
        return !clazz.getName().startsWith("java");
    }
}
