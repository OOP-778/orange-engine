package com.oop.orangeengine.database.gson;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.annotation.UniqueLabel;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassRegistry {
    // List of enums
    private static final Set<Class> enums = new HashSet<>();

    // Class to class struct
    private static final Map<Class, ClassStructure> classStructures = new HashMap<>();

    // Classes with unique labels
    private static final Map<String, Class> uniqueLabelClasses = new HashMap<>();

    // Cache found classes values, so it's faster for same type objects to find their clazz
    private static final Cache<OPair<Integer, Integer>, ClassStructure> structToClass = CacheBuilder
            .newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .build();

    // Cache found enums, so it's faster for the same value to find their enum
    private static final Cache<String, Enum> valueToEnum = CacheBuilder
            .newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .build();

    private static final Set<Pattern> ignoredPaths = new HashSet<>();

    public static void register(Class ...clazzs) {
        for (Class clazz : clazzs) {
            if (isIgnored(clazz)) continue;

            if (clazz.isEnum())
                enums.add(clazz);

            else {
                UniqueLabel uniqueLabel = (UniqueLabel) clazz.getAnnotation(UniqueLabel.class);
                if (uniqueLabel != null)
                    uniqueLabelClasses.put(uniqueLabel.label(), clazz);
                else
                    classStructures.put(clazz, new ClassStructure(clazz));
            }
        }
    }

    private static boolean isIgnored(Class clazz) {
        if (clazz.getName().contains("java") || clazz.getName().contains("google")) return true;

        boolean result[] = new boolean[]{false};
        ignoredPaths.forEach(pattern -> {
            if (result[0]) return;

            if (pattern.matcher(clazz.getName()).find())
                result[0] = true;
        });

        return result[0];
    }

    public static Enum byValue(String enumValue) {
        Enum ifPresent = valueToEnum.getIfPresent(enumValue);
        if (ifPresent != null)
            return ifPresent;

        return enums
                .stream()
                .map(enumClass -> {
                    try {
                        Method valuesMethod = OSimpleReflection.getMethod(enumClass, "values");
                        Object[] values = (Object[]) valuesMethod.invoke(null);

                        for (Object o : values) {
                            if (String.valueOf(o).equalsIgnoreCase(enumValue))
                                return o;
                        }

                        return null;
                    } catch (Throwable thrw) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(o -> (Enum) o)
                .peek(foundEnum -> valueToEnum.put(enumValue, foundEnum))
                .findFirst()
                .orElse(null);
    }

    public static Class byStruct(Set<String> keys) {
        ClassStructure ifPresent = structToClass.getIfPresent(new OPair<>(keys.hashCode(), keys.size()));
        if (ifPresent != null) {
            return ifPresent.getClazz();
        }

        return classStructures.values()
                .stream()
                .filter(struct -> struct.equals(keys))
                .peek(struct -> structToClass.put(new OPair<>(keys.hashCode(), keys.size()), struct))
                .findFirst()
                .map(ClassStructure::getClazz)
                .orElse(null);
    }

    public static void ignorePath(Pattern pattern) {
        ignoredPaths.add(pattern);
    }

    public static void registerHierarchy(Class ...clazzs) {
        for (Class clazz : clazzs) {
            register(clazz);
            register(ClassHierarchyGatherer.gatherer(clazz).gather().toArray(new Class[0]));
        }
    }

    public static boolean isRegistered(Class clazz) {
        return isIn(clazz, enums) || isIn(clazz, uniqueLabelClasses.values()) || isIn(clazz, classStructures.keySet());
    }

    private static boolean isIn(Class clazz, Collection<Class> classes) {
        return classes.stream().anyMatch(clazz2 -> clazz2.isAssignableFrom(clazz));
    }

    public static Class byUniqueIdentifier(String identifier) {
        return uniqueLabelClasses.get(identifier);
    }

    public static Set<Class> getRegisteredClasses() {
        return Stream
                .of(uniqueLabelClasses.values(), classStructures.keySet(), enums)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
