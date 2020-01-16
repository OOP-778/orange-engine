package com.oop.orangeengine.database.holder;

import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtil {
    private static Map<Class, List<OPair<Field, Column>>> cachedColumns = new ConcurrentHashMap<>();

    public static List<OPair<Field, Column>> getFields(Class klass, boolean withParents) {
        List<OPair<Field, Column>> klassValues = cachedColumns.get(klass);

        if (klassValues == null) {
            initCachedColumns(klass, withParents);
            return getFields(klass);

        } else {
            List<Class> parents = getAllParents(klass);
            if (parents.size() > 0)
                klassValues.addAll(getFields(parents.get(0)));

            klassValues.sort(Comparator.comparing(pair -> pair.getSecond().name()));
            return klassValues;
        }
    }

    public static List<OPair<Field, Column>> getFields(Class klass) {
        return getFields(klass, false);
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

    public static void initCachedColumns(Class holder) {
        initCachedColumns(holder, true);
    }

    private static void initCachedColumns(Class holder, boolean deep) {

        List<OPair<Field, Column>> holderValue = cachedColumns.get(holder);
        List<OPair<Field, Column>> parentValue = cachedColumns.get(holder.getSuperclass());

        if (deep)
            getAllParents(holder).forEach(klass -> DatabaseObject.initCachedColumns(klass, false));

        if (holderValue == null) {
            holderValue = new ArrayList<>();

            for (Field field : holder.getDeclaredFields()) {
                if (!field.isAccessible())
                    field.setAccessible(true);

                Column column = field.getAnnotation(Column.class);
                if (column == null) continue;

                holderValue.add(new OPair<>(field, column));
            }

            cachedColumns.put(holder, Collections.synchronizedList(holderValue));
        }

        if (parentValue == null && holder.getSuperclass() != null && holder.getSuperclass() != DatabaseObject.class) {
            parentValue = new ArrayList<>();

            for (Field field : holder.getSuperclass().getDeclaredFields()) {
                if (!field.isAccessible())
                    field.setAccessible(true);

                Column column = field.getAnnotation(Column.class);
                if (column == null) continue;

                parentValue.add(new OPair<>(field, column));
            }
            cachedColumns.put(holder.getSuperclass(), Collections.synchronizedList(parentValue));
        }
    }
}
