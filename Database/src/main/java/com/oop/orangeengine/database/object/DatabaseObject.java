package com.oop.orangeengine.database.object;

import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.data.DataHandlerController;
import com.oop.orangeengine.database.data.IDataHandler;
import com.oop.orangeengine.database.provider.ClassFieldProvider;
import com.oop.orangeengine.database.provider.FieldProviderController;
import com.oop.orangeengine.database.provider.IDBFieldProvider;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DatabaseObject {

    private static Map<Class, List<OPair<Field, DatabaseValue>>> cachedColumns = new ConcurrentHashMap<>();
    protected Map<String, Integer> hashCodes = new ConcurrentHashMap<>();
    private Class holder;

    @Getter
    private int rowId = -1;

    private List<Runnable> whenLoaded = new LinkedList<>();

    private DataController dataController;

    public DatabaseObject() {
        this.holder = this.getClass();
        initCachedColumns(holder);
    }

    public void load(DataController dataController, int rowId) {
        ODatabase database = dataController.getDatabase();
        try {

            /*
            1. We need to gather the list of available fields in table
            2. Deserialize them & load
            3. Supply missing field values
            */

            this.rowId = rowId;
            this.dataController = dataController;

            DatabaseTable table = findTable(holder);
            if (table == null)
                throw new IllegalStateException("Failed to load " + holder.getSimpleName() + " cause of missing DatabaseTable annotation");

            List<OPair<Field, DatabaseValue>> cachedColumns = new ArrayList<>(getFields(holder));
            for (OPair<Field, DatabaseValue> cachedColumn : cachedColumns) {
                Object fieldValue = cachedColumn.getFirst().get(this);
                if (!database.tableHasValue(table.tableName(), cachedColumn.getSecond().columnName(), rowId) && fieldValue == null) {

                    ClassFieldProvider classFieldProvider = FieldProviderController.getInstance().findProvider(holder);
                    if (classFieldProvider == null)
                        throw new IllegalStateException("Failed to load database object field column " + cachedColumn.getSecond().columnName() + " cause Class Provider wasn't found!");

                    IDBFieldProvider fieldProvider = classFieldProvider.findProvider(cachedColumn.getSecond().columnName());
                    if (fieldProvider == null)
                        throw new IllegalStateException("Failed to load database object field column " + cachedColumn.getSecond().columnName() + " cause Field Provider wasn't found!");

                    Object value = fieldProvider.provide(this);
                    cachedColumn.getFirst().set(this, value);
                    hashCodes.put(cachedColumn.getSecond().columnName(), value.hashCode());
                    continue;
                }

                Object object = database.gatherColumnValue(table.tableName(), cachedColumn.getSecond(), "id", rowId + "");
                if (object == null && fieldValue == null)
                    throw new IllegalStateException("Failed to load database value from table: " + table.tableName() + ", column: " + cachedColumn.getSecond().columnName() + ", rowID: " + rowId);

                if(fieldValue != null && object == null)
                    continue;

                Field field = cachedColumn.getFirst();
                if (object.toString().contentEquals("null")) {
                    field.set(this, null);
                    continue;
                }

                if (objectTypeMatches(field.getType(), object.getClass())) {
                    field.set(this, object);
                    hashCodes.put(cachedColumn.getSecond().columnName(), object.hashCode());

                } else {

                    // We have to wrap object into required value aka deserialize
                    DataHandlerController dhm = DataHandlerController.getInstance();
                    IDataHandler dataHandler = dhm.findDataHandler(field.getType());
                    if (dataHandler == null)
                        throw new IllegalStateException("Failed to load database value (table: " + table.tableName() + ", column: " + cachedColumn.getSecond().columnName() + ", rowID: " + rowId + ") cause IDataHandler is not found for type " + field.getType());

                    Object loadedValue = dataHandler.load(object.toString(), field.getType());
                    field.set(this, loadedValue);
                    hashCodes.put(cachedColumn.getSecond().columnName(), loadedValue.hashCode());

                }
            }
            whenLoaded.forEach(Runnable::run);

        } catch (Exception ex) {
           ex.printStackTrace();
        }
    }

    private boolean objectTypeMatches(Class k1, Class k2) {
        return k1 == Integer.class || k1 == int.class && (k2 == Integer.class || k2 == int.class) ||
                k1 == Double.class || k1 == double.class && (k2 == Double.class || k2 == double.class) ||
                k1 == Float.class || k1 == float.class && (k2 == Float.class || k2 == float.class) ||
                k1 == Boolean.class || k1 == boolean.class && (k2 == Boolean.class || k2 == boolean.class) ||
                k1 == k2;

    }

    private DatabaseTable findTable(Class klass) {

        // Try to find klass annotation
        DatabaseTable databaseTable = (DatabaseTable) klass.getDeclaredAnnotation(DatabaseTable.class);

        if (databaseTable == null)
            throw new IllegalStateException("Failed to find DatabaseTable.class annotation for class " + klass.getSimpleName());

        return databaseTable;

    }

    private List<Field> mergeSuperClassFields(Class klass) {

        List<Field> fields = new LinkedList<>();

        // Add declared source fields
        fields.addAll(Arrays.asList(klass.getDeclaredFields()));

        // Add declared source superclass fields
        if (klass.getSuperclass() != null)
            fields.addAll(Arrays.asList(klass.getSuperclass().getDeclaredFields()));

        fields.forEach(field -> field.setAccessible(true));

        return fields;

    }

    public static void initCachedColumns(Class holder) {
        initCachedColumns(holder, true);
    }

    public static void initCachedColumns(Class holder, boolean deep) {

        // In order to save some memory, store parent fields separately
        List<OPair<Field, DatabaseValue>> holderValue = cachedColumns.get(holder);
        List<OPair<Field, DatabaseValue>> parentValue = cachedColumns.get(holder.getSuperclass());

        if (deep)
            getAllParents(holder).forEach(klass -> DatabaseObject.initCachedColumns(klass, false));

        if (holderValue == null) {
            holderValue = new ArrayList<>();

            for (Field field : holder.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) continue;

                field.setAccessible(true);
                DatabaseValue databaseValue = field.getDeclaredAnnotation(DatabaseValue.class);
                if (databaseValue == null) continue;

                holderValue.add(new OPair<>(field, databaseValue));

            }
            cachedColumns.put(holder, Collections.synchronizedList(holderValue));

        }

        if (parentValue == null && holder.getSuperclass() != null && holder.getSuperclass() != DatabaseObject.class) {
            parentValue = new ArrayList<>();

            for (Field field : holder.getSuperclass().getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) continue;

                DatabaseValue databaseValue = field.getDeclaredAnnotation(DatabaseValue.class);
                if (databaseValue == null) continue;

                parentValue.add(new OPair<>(field, databaseValue));

            }
            cachedColumns.put(holder.getSuperclass(), Collections.synchronizedList(parentValue));
        }

    }

    public Object wrapFieldObject(Field field) throws IllegalAccessException {

        Object value = field.get(this);
        if (value == null)
            return "null";

        if (isRaw(value))
            return value;

        else {

            // We have to serialize the object
            DataHandlerController dhm = DataHandlerController.getInstance();
            IDataHandler dataHandler = dhm.findDataHandler(value.getClass());
            if (dataHandler == null)
                throw new IllegalStateException("Failed to find Data Handler for object type " + field.getType().getSimpleName());

            return dataHandler.serialize(value);
        }

    }

    private boolean isRaw(Object object) {

        Class klass = object.getClass();
        return klass == int.class ||
                klass == Integer.class ||

                klass == Double.class ||
                klass == double.class ||

                klass == Float.class ||
                klass == float.class ||

                klass == Long.class ||
                klass == long.class ||

                klass == String.class;
    }

    public static List<OPair<Field, DatabaseValue>> getFields(Class klass) {

        List<OPair<Field, DatabaseValue>> klassValues = cachedColumns.get(klass);
        if (klassValues == null) {
            initCachedColumns(klass);
            return getFields(klass);

        } else {
            List<Class> parents = getAllParents(klass);
            if (parents.size() > 0)
                klassValues.addAll(getFields(parents.get(0)));

            klassValues.sort(Comparator.comparing(pair -> pair.getSecond().columnName()));
            return klassValues;
        }

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

    public static Map<Class, List<OPair<Field, DatabaseValue>>> getCachedColumns() {
        return cachedColumns;
    }

    void setRowId(int rowCount) {
        this.rowId = rowCount;
    }

    public void save(boolean async) {
        if (dataController != null)
            dataController.save(this, async);
    }

    public void save() {
        save(false);
    }

    public void setWhenLoaded(Runnable runnable) {
        this.whenLoaded.add(runnable);
    }
}
