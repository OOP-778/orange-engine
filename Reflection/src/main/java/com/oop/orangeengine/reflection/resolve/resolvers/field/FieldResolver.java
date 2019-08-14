package com.oop.orangeengine.reflection.resolve.resolvers.field;

import com.oop.orangeengine.reflection.OClass;
import com.oop.orangeengine.reflection.resolve.resolved.ResolvedField;
import com.oop.orangeengine.reflection.resolve.resolvers.AResolver;

import java.lang.reflect.Field;
import java.util.Optional;

public class FieldResolver extends AResolver<ResolvedField> {

    private String[] names;

    public FieldResolver(OClass holder) {
        super(holder);
    }

    public FieldResolver withNames(String... names) {
        this.names = names;
        return this;
    }

    @Override
    public Optional<ResolvedField> resolve() {

        if (getHolder() == null || names == null)
            handleException(new NullPointerException("Holder class or Names are null!"));

        Field field = null;

        for(String fieldName : names) {

            try {
                field = getJavaHolder().getField(fieldName);
            } catch (Exception e) {
                if(e instanceof NoSuchFieldException) continue;
                handleException(e);
            }

        }

        if(field!= null)
            return Optional.of(getHolder().registerField(field));

        return Optional.empty();

    }
}
