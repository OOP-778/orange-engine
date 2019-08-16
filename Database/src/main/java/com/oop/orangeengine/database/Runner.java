package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runner {

    public static void main(String[] args) {

        ODatabse database = null;

        Integer value = 1;
        System.out.println(value.hashCode());

        value = 5;
        System.out.println(value.hashCode());

    }

    public static DatabaseTable findTable(Class klass) {

        //Try to find klass annotation
        DatabaseTable databaseTable = (DatabaseTable) klass.getDeclaredAnnotation(DatabaseTable.class);

        if (databaseTable == null)
            databaseTable = (DatabaseTable) klass.getSuperclass().getDeclaredAnnotation(DatabaseTable.class);

        return databaseTable;

    }

    public static List<Field> mergeSuperClassFields(Class klass) {

        List<Field> fields = new ArrayList<>();

        //Add declared source fields
        fields.addAll(Arrays.asList(klass.getDeclaredFields()));

        //Add declared source superclass fields
        if(klass.getSuperclass() != null)
            fields.addAll(Arrays.asList(klass.getSuperclass().getDeclaredFields()));

        fields.forEach(field -> field.setAccessible(true));

        return fields;

    }

}
