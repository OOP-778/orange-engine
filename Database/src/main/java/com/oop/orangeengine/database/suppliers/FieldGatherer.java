package com.oop.orangeengine.database.suppliers;

import com.oop.orangeengine.database.DatabaseObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.oop.orangeengine.database.util.ClassUtil.runWithObject;

@Accessors(chain = true, fluent = true)
@Data
public class FieldGatherer {

    private FieldGatherer() {}

    public static FieldGatherer create() {
        return new FieldGatherer();
    }

    private Predicate<Field> filter;

    public Set<Field> gather(Class<?> clazz) {
        List<Class> parents = getAllParents(clazz);
        parents.add(clazz);

        return parents
                .stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                .filter(field -> filter != null && filter.test(field))
                .map(field -> runWithObject(field, (field2) -> field.setAccessible(true)))
                .collect(Collectors.toSet());
    }

    private List<Class> getAllParents(Class klass) {
        List<Class> parents = new ArrayList<>();
        klass = klass.getSuperclass();

        while (klass != null && !klass.getSimpleName().equalsIgnoreCase("Object") && klass != DatabaseObject.class) {
            parents.add(klass);
            klass = klass.getSuperclass();
        }

        return parents;
    }
}
