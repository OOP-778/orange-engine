package com.oop.orangeengine.database.suppliers.serializable;

import com.google.common.cache.Cache;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.database.suppliers.FieldGatherer;
import com.oop.orangeengine.database.suppliers.Suppliable;

import java.lang.reflect.Field;

public interface SerializableObject extends Suppliable {

    @Override
    default void initFields(Cache<String, Field> cache) {
        FieldGatherer.create()
                .filter(field -> field.getAnnotation(SerializedName.class) != null)
                .gather(getClass())
                .forEach(field -> {
                    SerializedName serialized = field.getAnnotation(SerializedName.class);
                    cache.put(serialized.value().toLowerCase(), field);
                });
    }
}
