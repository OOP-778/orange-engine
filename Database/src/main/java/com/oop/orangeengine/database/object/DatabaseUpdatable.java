package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.main.util.data.map.OMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface DatabaseUpdatable {
    final OMap<DatabaseValue, Field> __fieldMap = new OMap<>();
    final OMap<String, Supplier<?>> __suppliersMap = new OMap<>();

    default void loadFields() {
        for (Field field : Arrays.stream(getClass().getFields()).filter(field -> field.isAnnotationPresent(DatabaseValue.class)).collect(Collectors.toList()))
            __fieldMap.put(field.getAnnotation(DatabaseValue.class), field);
    }

    default boolean has(String databaseValue) {
        return __fieldMap.keySet().stream()
                .anyMatch(value -> value.columnName().contentEquals(databaseValue));
    }

    default Optional<Class<?>> valueClazz(String databaseValue) {
        Field field = __fieldMap.get(databaseValue);
        if (field == null) return Optional.empty();

        return Optional.of(field.getType());
    }

    default <O> void registerFieldSupplier(String databaseValue, Class<O> type, Supplier<O> supplier) {
        if (has(databaseValue)) {
            Optional<Class<?>> valueClazz = valueClazz(databaseValue);
            if (!valueClazz.isPresent()) return;

            if (valueClazz.get().isAssignableFrom(type))
                __suppliersMap.put(databaseValue, supplier);
        }
    }

    default void updateFields() {
        __fieldMap.forEach((value, field) -> {
            try {
                if (field.get(this) != null) return;
                Supplier<?> supplier = __suppliersMap.get(value.columnName());

                field.set(this, supplier.get());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
