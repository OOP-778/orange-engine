package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.ODatabse;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.data.DataHandlerManager;
import com.oop.orangeengine.database.data.IDataHandler;
import com.oop.orangeengine.main.util.pair.OPair;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class DatabaseObject<T> {

    private Class<T> holder;
    private int rowId;

    public DatabaseObject(Class<T> holder) {
        this.holder = holder;
    }

    public void load(ODatabse databse) {
        try {

            /*
            1. We need to gather the list of available fields in table
            2. Deserialize them & load
            3. Supply missing field values
            */

            //Need to look for DatabaseTable
            DatabaseTable table = findTable(holder);
            if (table == null)
                throw new IllegalStateException("Failed to load DatabaseObject cause of missing DatabaseTable annotation");

            List<OPair<Field, DatabaseValue>> requiredColumns = new ArrayList<>();
            for (Field field : mergeSuperClassFields(holder)) {

                DatabaseValue databaseValue = field.getDeclaredAnnotation(DatabaseValue.class);
                if (databaseValue != null)
                    requiredColumns.add(new OPair<>(field, databaseValue));

            }

            for (OPair<Field, DatabaseValue> requiredColumn : requiredColumns) {

                Object object = databse.gatherColumnValue(table.tableName(), requiredColumn.getSecond().columnName(), "id", rowId + "");
                if (object == null)
                    // TODO Add missing value provider lookup, first we need to find if the value is actually missing or it's just database issue
                    throw new IllegalStateException("Failed to load database value from table: " + table.tableName() + ", column: " + requiredColumn.getSecond().columnName() + ", rowID: " + rowId);

                Field field = requiredColumn.getFirst();
                if(field.getType() == object.getClass())
                    field.set(this, object);

                else {

                    // We have to wrap object into required value aka deserialize
                    DataHandlerManager dhm = DataHandlerManager.getInstance();
                    IDataHandler dataHandler = dhm.findDataHandler(field.getType());
                    if (dataHandler == null)
                        throw new IllegalStateException("Failed to load database value (table: " + table.tableName() + ", column: " + requiredColumn.getSecond().columnName() + ", rowID: " + rowId + ") cause IDataHandler is not found for type " + field.getType());

                    field.set(this, dataHandler.load(object.toString()));

                }

            }


        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private DatabaseTable findTable(Class klass) {

        //Try to find klass annotation
        DatabaseTable databaseTable = (DatabaseTable) klass.getDeclaredAnnotation(DatabaseTable.class);

        if (databaseTable == null && klass.getSuperclass() != null)
            databaseTable = (DatabaseTable) klass.getSuperclass().getDeclaredAnnotation(DatabaseTable.class);

        return databaseTable;

    }

    private List<Field> mergeSuperClassFields(Class klass) {

        List<Field> fields = new ArrayList<>();

        //Add declared source fields
        fields.addAll(Arrays.asList(klass.getDeclaredFields()));

        //Add declared source superclass fields
        if (klass.getSuperclass() != null)
            fields.addAll(Arrays.asList(klass.getSuperclass().getDeclaredFields()));

        fields.forEach(field -> field.setAccessible(true));

        return fields;

    }


}
