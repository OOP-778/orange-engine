package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runner {

    public static void main(String[] args) {

        ODatabse database = null;
        TestingObject testingObject = new TestingObjectB();

        DatabaseTable table = findTable(testingObject.getClass());
        if (table == null) return;

        try {

            boolean first = true;

            for (Field field : mergeSuperClassFields(testingObject.getClass())) {

                DatabaseValue databaseValue = field.getDeclaredAnnotation(DatabaseValue.class);
                if (databaseValue == null) continue;

                if (first) {

                    database.newTableCreator()
                            .setName(table.tableName())
                            .addColumn(databaseValue.columnName(), databaseValue.rowType())
                            .create();
                    first = false;
                    continue;

                }



            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        fields.addAll(Arrays.asList(klass.getSuperclass().getDeclaredFields()));

        fields.forEach(field -> field.setAccessible(true));

        return fields;

    }

}
