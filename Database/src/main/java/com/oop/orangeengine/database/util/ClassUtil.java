package com.oop.orangeengine.database.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassUtil {
    private static Cache<Class, List<Field>> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .build();

    public static List<Field> getFields(Class klass) {
        return getAllFields(klass);
    }

    public static Optional<Field> getField(Class klass, Predicate<Field> predicate) {
        return getAllFields(klass).stream().filter(predicate).findFirst();
    }

    private static List<Class> getAllParents(Class klass) {
        List<Class> parents = new ArrayList<>();
        klass = klass.getSuperclass();

        while (klass != null && !klass.getSimpleName().equalsIgnoreCase("Object") && klass != DatabaseObject.class) {
            parents.add(klass);
            klass = klass.getSuperclass();
        }

        return parents;
    }

    private static List<Field> getAllFields(Class klass) {
        List<Class> classes = getAllParents(klass);
        classes.add(klass);

        Set<Field> nonDuplicates = new LinkedHashSet<>();
        for (Class clazz : classes) {
            List<Field> cacheValue = cache.getIfPresent(clazz);
            if (cacheValue == null)
                nonDuplicates.addAll(initClass(clazz));

            else
                nonDuplicates.addAll(cacheValue);
        }

        return new ArrayList<>(nonDuplicates);
    }

    private static List<Field> initClass(Class klass) {
        List<Field> fieldList = Arrays
                .stream(klass.getDeclaredFields())
                .map(field -> runWithObject(field, field2 -> field2.setAccessible(true)))
                .filter(field -> field.getAnnotation(Column.class) != null || field.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());
        cache.put(klass, fieldList);

        return fieldList;
    }

    public static <T> T runWithObject(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
        return obj;
    }
}
