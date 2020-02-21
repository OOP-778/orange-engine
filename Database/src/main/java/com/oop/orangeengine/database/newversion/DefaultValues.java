package com.oop.orangeengine.database.newversion;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class DefaultValues {
    /**
     * @param clazz the class for which a default value is needed
     * @return A reasonable default value for the given class (the boxed default
     * value for primitives, <code>null</code> otherwise).
     */
    @SuppressWarnings("unchecked")
    public static <T> T forClass(Class<T> clazz) {
        return (T) DEFAULT_VALUES.get(clazz);
    }

    private static final Map<Class<?>, Object> DEFAULT_VALUES = Stream
            .of(boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class)
            .collect(toMap(clazz -> (Class<?>) clazz, clazz -> Array.get(Array.newInstance(clazz, 1), 0)));

    public static void main(String... args) {
        System.out.println(DefaultValues.forClass(int.class)); // 0
        System.out.println(DefaultValues.forClass(Integer.class)); // null
    }
}