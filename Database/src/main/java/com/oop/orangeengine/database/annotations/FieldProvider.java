package com.oop.orangeengine.database.annotations;

import com.oop.orangeengine.database.data.IDBFieldProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldProvider {

    Class<? extends IDBFieldProvider> provider();

}
