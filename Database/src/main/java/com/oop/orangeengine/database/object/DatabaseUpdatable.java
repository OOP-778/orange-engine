package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.main.util.data.map.OConcurrentMap;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface DatabaseUpdatable {
    final Map<Class, Map<DatabaseValue, Field>> __fieldMap = new OConcurrentMap<>();
    final Map<Class, Map<String, Supplier<?>>> __suppliersMap = new OConcurrentMap<>();

    default void loadFields() {
        for (Field field : __getFields(getClass()).stream()
                .filter(field -> field.isAnnotationPresent(DatabaseValue.class))
                .collect(Collectors.toList())) {
            if (!field.isAccessible()) field.setAccessible(true);

            Map<DatabaseValue, Field> classMap = __fieldMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>());
            classMap.put(field.getAnnotation(DatabaseValue.class), field);
        }
    }

    default boolean has(String databaseValue) {
        Map<DatabaseValue, Field> classMap = __fieldMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>());

        return classMap.keySet().stream()
                .anyMatch(value -> value.columnName().toLowerCase().contentEquals(databaseValue.toLowerCase()));
    }

    default Optional<Class<?>> valueClazz(String databaseValue) {
        Map<DatabaseValue, Field> classMap = __fieldMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>());

        Optional<DatabaseValue> fieldValue = classMap.keySet().stream().filter(value -> value.columnName().toLowerCase().contentEquals(databaseValue.toLowerCase())).findFirst();
        if (!fieldValue.isPresent()) return Optional.empty();

        Field field = classMap.get(fieldValue.get());
        if (field == null) return Optional.empty();

        return Optional.of(field.getType());
    }

    default <O> void registerFieldSupplier(String databaseValue, Class<O> type, Supplier<O> supplier) {
        if (has(databaseValue)) {
            Optional<Class<?>> valueClazz = valueClazz(databaseValue);
            if (!valueClazz.isPresent()) return;

            if (valueClazz.get().isAssignableFrom(type))
                __suppliersMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>()).put(databaseValue.toLowerCase(), supplier);
        }
    }

    default void updateFields() {
        Map<DatabaseValue, Field> classMap = __fieldMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>());
        Map<String, Supplier<?>> classSuppliers = __suppliersMap.computeIfAbsent(getClass(), (clazz) -> new OConcurrentMap<>());

        classMap.forEach((value, field) -> {
            try {
                if (field.get(this) != null) return;
                Supplier<?> supplier = classSuppliers.get(value.columnName().toLowerCase());
                if (supplier == null) return;

                field.set(this, supplier.get());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    default List<Field> __getFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class tmpClass = clazz;
        while (tmpClass != null) {
            fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
            tmpClass = tmpClass.getSuperclass();
        }

        return fieldList;
    }
}
