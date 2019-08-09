package com.oop.orangeEngine.reflection.resolve.resolvers.field;

import com.oop.orangeEngine.reflection.OClass;
import com.oop.orangeEngine.reflection.resolve.resolved.ResolvedField;
import com.oop.orangeEngine.reflection.resolve.resolvers.AResolvers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FieldsResolver extends AResolvers<ResolvedField> {

    public FieldsResolver(OClass holder) {
        super(holder);
    }

    public FieldsResolver resolve(String... fieldName) {
        return resolve(new HashSet<String>(){{
            addAll(Arrays.asList(fieldName));
        }});
    }

    public FieldsResolver resolve(Set<String> resolvable) {
        Field field = null;

        for (String fieldName : resolvable) {

            try {
                field = getJavaHolder().getField(fieldName);
            } catch (Exception e) {
                if(e instanceof NoSuchFieldException) continue;
                handleException(e);
            }

            if(field != null) {

                ResolvedField resolvedField = getHolder().registerField(field);
                getResolved().offer(resolvedField);

            }

        }

        return this;

    }

}
