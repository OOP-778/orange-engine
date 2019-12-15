package t.a;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface GsonUpdateable {

    final static Map<SerializedName, Field> __fieldMap = new HashMap<>();
    final static Map<String, Supplier<?>> __suppliersMap = new HashMap<>();

    default void loadFields() {
        for (Field field : Arrays.stream(getClass().getFields()).filter(field -> field.isAnnotationPresent(SerializedName.class)).collect(Collectors.toList()))
            __fieldMap.putIfAbsent(field.getAnnotation(SerializedName.class), field);
    }

    default boolean has(String serializedName) {
        return __fieldMap.keySet().stream()
                .anyMatch(value -> value.value().contentEquals(serializedName));
    }

    default Optional<Class<?>> valueClazz(String serializedName) {
        Field field = __fieldMap.get(serializedName);
        if (field == null) return Optional.empty();

        return Optional.of(field.getType());
    }

    default <O> void registerFieldSupplier(String serializedName, Class<O> type, Supplier<O> supplier) {
        if (has(serializedName)) {
            Optional<Class<?>> valueClazz = valueClazz(serializedName);
            if (!valueClazz.isPresent()) return;

            if (valueClazz.get().isAssignableFrom(type))
                __suppliersMap.put(serializedName, supplier);
        }
    }

    default void updateFields() {
        __fieldMap.forEach((value, field) -> {
            try {
                if (field.get(this) != null) return;
                Supplier<?> supplier = __suppliersMap.get(value.value());
                if (supplier == null) {
                    // TODO: Throw
                    return;
                }

                field.set(this, supplier.get());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
