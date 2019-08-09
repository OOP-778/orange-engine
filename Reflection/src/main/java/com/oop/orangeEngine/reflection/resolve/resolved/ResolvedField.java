package com.oop.orangeEngine.reflection.resolve.resolved;

import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Field;

@Getter
public class ResolvedField {

    private Field javaField;

    public ResolvedField(@NonNull Field javafield) {
        this.javaField = javafield;
        javafield.setAccessible(true);
    }

}
