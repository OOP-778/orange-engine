package com.oop.orangeengine.database.suppliers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.oop.orangeengine.database.DatabaseField;
import com.oop.orangeengine.database.util.DefaultValues;
import org.apache.commons.lang.math.NumberUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Suppliable {

    final Map<Class<?>, Map<String, Supplier<?>>> _suppliers = Maps.newConcurrentMap();
    final Map<Class<?>, Cache<String, Field>> _fieldCache = Maps.newConcurrentMap();

    default void _loadSupplier() {
        Map<String, Supplier<?>> supplierMap = _suppliers.get(getClass());
        if (supplierMap == null || supplierMap.isEmpty()) return;

        Cache<String, Field> fieldCache = _fieldCache.get(getClass());
        if (fieldCache.size() == 0)
            initFields(fieldCache);

        fieldCache.getAllPresent(supplierMap.keySet()).forEach((name, field) -> {
            try {
                Object o = field.get(this);
                if (o instanceof DatabaseField<?> && isDefault(((DatabaseField<?>) o).get()))
                    ((DatabaseField) o).set(supplierMap.get(name).get());

                else if (isDefault(o))
                    field.set(this, supplierMap.get(name).get());
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to update object" + getClass().getSimpleName() + ", cause: ", thrw);
            }
        });
    }

    default <O> void _registerSupplier(String name, Class<O> clazz, Supplier<O> supplier) {
        _registerSupplier(getClass(), name, supplier);
    }

    default void _registerSupplier(Class<?> clazz, String name, Supplier<?> supplier) {
        Map<String, Supplier<?>> supplierMap = _suppliers.computeIfAbsent(clazz, (c) -> Maps.newConcurrentMap());
        Cache<String, Field> fieldCache = _fieldCache.computeIfAbsent(clazz, (c) -> CacheBuilder.newBuilder().concurrencyLevel(4).expireAfterAccess(15, TimeUnit.SECONDS).build());
        if (fieldCache.size() == 0)
            initFields(fieldCache);

        supplierMap.remove(name.toLowerCase());
        supplierMap.put(name.toLowerCase(), supplier);
    }

    default <O> void _registerSupplier(Class<?> clazz, String name, Class<O> objectClazz, Supplier<O> supplier) {
        _registerSupplier(clazz, name, supplier);
    }

    default boolean _hasSupplier(Class<?> clazz, String name) {
        Map<String, Supplier<?>> supplierMap = _suppliers.get(clazz);
        return supplierMap != null && supplierMap.containsKey(name.toLowerCase());
    }

    void initFields(Cache<String, Field> cache);

    default boolean isDefault(Object object) {
        if (object == null) return true;

        Object def = DefaultValues.forClass(object.getClass());
        if (def != null && def == object)
            return true;

        return NumberUtils.toDouble(String.valueOf(object), 0) == 0;
    }
}
