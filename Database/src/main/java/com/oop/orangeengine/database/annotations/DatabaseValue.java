package com.oop.orangeengine.database.annotations;

import com.oop.orangeengine.database.OColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseValue {

    String columnName();

    OColumn columnType() default OColumn.TEXT;


}
