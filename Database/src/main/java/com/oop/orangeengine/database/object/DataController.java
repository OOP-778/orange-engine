package com.oop.orangeengine.database.object;

import com.google.common.collect.HashBiMap;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.DataModificationHandler;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import lombok.Getter;

import javax.xml.stream.events.StartDocument;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.oop.orangeengine.main.Engine.getEngine;

public abstract class DataController {

    private HashBiMap<String, Class<? extends DatabaseObject>> classToTable = HashBiMap.create();

    @Getter
    private ODatabase database;

    private boolean autoSave;

    public DataController(ODatabase database) {
        this.database = database;
    }

    @Getter
    private OConcurrentSet<DatabaseObject> data = new OConcurrentSet<>();

    public void load() {
        for (String tableName : classToTable.keySet()) {

            updateTable(classToTable.get(tableName));
            List<Integer> rowIds = database.getRowIds(tableName);

            rowIds.parallelStream().forEach(rowId -> {
                try {
                    Class klass = classToTable.get(tableName);
                    Constructor<? extends DatabaseObject> declaredConstructor = null;
                    try {
                        declaredConstructor = klass.getDeclaredConstructor();
                    } catch (NoSuchMethodException ex) {
                        getEngine().getLogger().error(ex, "Failed to find Constructor for class " + klass.getSimpleName() + ". Contact Developer immediately!");
                    }
                    if (declaredConstructor == null)
                        throw new IllegalStateException("Failed to find no args constructor for class " + klass.getName());

                    declaredConstructor.setAccessible(true);
                    DatabaseObject value = declaredConstructor.newInstance();
                    value.load(this, rowId);
                    data.add(value);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        }
    }

    public void registerClass(Class<? extends DatabaseObject> klass) {
        DatabaseTable table = klass.getDeclaredAnnotation(DatabaseTable.class);
        if (table == null)
            throw new IllegalStateException("Failed to register Data Type, cause table name wasn't found in class: " + klass.getSimpleName());

        classToTable.put(table.tableName(), klass);
    }

    public void save(DatabaseObject object, boolean async) {
        if (async)
            StaticTask.getInstance().async(() -> save(object));

        else
            StaticTask.getInstance().sync(() -> save(object));
    }

    public void save(DatabaseObject object) {
        save(new HashSet<DatabaseObject>() {{
            add(object);
        }});
    }


    public void saveAll() {
        save(data);
    }

    public void save(Collection<DatabaseObject> objects) {

        /*
        1. Because this controller can store any DatabaseObject we have to split objects into own maps of class & update table structure
        2. Save objects
        */

        try (Connection connection = database.getConnection()) {
            Map<Class, List<DatabaseObject>> splittedObjects = new HashMap<>();
            connection.setAutoCommit(false);

            for (DatabaseObject object : objects) {

                List<DatabaseObject> classObjects = splittedObjects.get(object.getClass());
                if (classObjects == null) {

                    classObjects = new ArrayList<>();
                    classObjects.add(object);
                    splittedObjects.put(object.getClass(), classObjects);

                    updateTable(object.getClass());

                } else
                    classObjects.add(object);
            }

            for (Map.Entry<Class, List<DatabaseObject>> entry : splittedObjects.entrySet()) {
                Class key = entry.getKey();
                List<DatabaseObject> value = entry.getValue();

                for (DatabaseObject object : value) {
                    if (object.getRowId() == -1) {
                        object.setRowId(database.getRowCount(classToTable.inverse().get(key)));
                        insertObject(object, connection);

                    } else
                        updateObject(object, connection);

                }
            }

            connection.commit();

        } catch (SQLException | IllegalAccessException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void insertObject(DatabaseObject object, Connection connection) throws SQLException {
        DatabaseTable table = object.getClass().getDeclaredAnnotation(DatabaseTable.class);
        assert table != null;

        StringBuilder stringBuilder = new StringBuilder()
                .append("INSERT INTO ")
                .append(table.tableName())
                .append(" (");

        List<OPair<Field, DatabaseValue>> fields = DatabaseObject.getFields(object.getClass());
        final boolean[] first = {true};
        for (OPair<Field, DatabaseValue> field : fields) {
            if (!first[0])
                stringBuilder.append(", ").append(field.getSecond().columnName());
            else {
                stringBuilder.append(field.getSecond().columnName());
                first[0] = false;
            }
        }
        stringBuilder.append(") VALUES (");
        first[0] = true;

        IntStream.range(1, fields.size() + 1).forEach(slot -> {
            if (!first[0])
                stringBuilder.append(", ?");

            else {
                stringBuilder.append("?");
                first[0] = false;
            }
        });

        stringBuilder.append(")");

        try (PreparedStatement statement = connection.prepareStatement(stringBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {
            IntStream.range(1, fields.size() + 1).forEach(slot -> {
                try {
                    statement.setObject(slot, object.wrapFieldObject(fields.get(slot - 1).getFirst()));
                } catch (SQLException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next())
                    object.setRowId(resultSet.getInt(1));
            }
        }
    }

    private void updateObject(DatabaseObject object, Connection connection) throws SQLException, IllegalAccessException {
        DatabaseTable table = object.getClass().getDeclaredAnnotation(DatabaseTable.class);
        assert table != null;

        List<OPair<Field, DatabaseValue>> fields = new LinkedList<>(DatabaseObject.getFields(object.getClass()));
        StringBuilder stringBuilder = new StringBuilder()
                .append("UPDATE ")
                .append(table.tableName())
                .append(" SET ");

        final boolean[] first = {true};

        fields.removeIf(field -> {

            Integer lastHashCode = object.hashCodes.get(field.getSecond().columnName());
            Object value = null;
            try {
                value = field.getFirst().get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value == null) return true;

            int currentHashCode = value.hashCode();
            if (lastHashCode != null) {
                object.hashCodes.remove(field.getSecond().columnName());
                object.hashCodes.put(field.getSecond().columnName(), currentHashCode);

                if (currentHashCode == lastHashCode) {
                    return true;
                }

            }
            object.hashCodes.put(field.getSecond().columnName(), currentHashCode);
            return false;

        });

        if (fields.isEmpty()) return;

        for (OPair<Field, DatabaseValue> field : fields) {
            if (!first[0])
                stringBuilder.append(", ").append(field.getSecond().columnName()).append(" = ?");

            else {
                stringBuilder.append(field.getSecond().columnName()).append(" = ?");
                first[0] = false;
            }
        }

        stringBuilder.append(" WHERE id = ").append(object.getRowId());

        try (PreparedStatement statement = connection.prepareStatement(stringBuilder.toString())) {
            int slot = 1;
            for (OPair<Field, DatabaseValue> field : fields) {

                statement.setObject(slot, object.wrapFieldObject(field.getFirst()));
                slot++;

            }
            statement.executeUpdate();
        }
    }

    public void updateTable(Class<? extends DatabaseObject> klass) {
        DatabaseTable table = klass.getDeclaredAnnotation(DatabaseTable.class);
        assert table != null;

        List<OPair<Field, DatabaseValue>> fields = new ArrayList<>(DatabaseObject.getFields(klass));
        if (!database.getTables().contains(table.tableName())) {

            ODatabase.TableCreator tableCreator = database.newTableCreator();
            tableCreator.of(table.tableName(), fields);
            tableCreator.create();

            return;
        }

        List<String> foundColumns = database.getColumns(table.tableName());

        // Gather all the required columns that are missing
        ODatabase.TableEditor tableEditor = database.newTableEditor().setName(table.tableName());
        for (OPair<Field, DatabaseValue> field : fields)
            if (!foundColumns.contains(field.getSecond().columnName()))
                tableEditor.addColumn(field.getSecond().columnName(), field.getSecond().columnType());

        // Update table structure
        tableEditor.edit();
    }

    public <T> Set<T> getData(Class<T> asKlass) {
        return data.stream()
                .filter(object -> asKlass.isAssignableFrom(object.getClass()))
                .map(object -> (T) object)
                .collect(Collectors.toSet());
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        if (autoSave)
            data.setHandler(new DataModificationHandler<DatabaseObject>() {
                @Override
                public void onAdd(DatabaseObject object) {
                    save(object);
                }

                @Override
                public void onRemove(DatabaseObject object) {
                    if (object.getRowId() != -1) {
                        DatabaseTable table = object.getClass().getDeclaredAnnotation(DatabaseTable.class);
                        assert table != null;

                        database.execute("DELETE FROM " + table.tableName() + " WHERE id = " + object.getRowId());
                    }
                }
            });
    }

    public <T extends DatabaseObject> void removeIf(Class<T> type, Predicate<T> predicate) {
        getData(type).stream()
                .filter(predicate)
                .findFirst()
                .ifPresent(object -> {
                    if (object.getRowId() != -1) {
                        DatabaseTable table = object.getClass().getDeclaredAnnotation(DatabaseTable.class);
                        assert table != null;

                        database.execute("DELETE FROM " + table.tableName() + " WHERE id = '" + object.getRowId() + "'");
                    }
                    getData().removeWithoutCheck(object);
                });
    }

    public <T extends DatabaseObject> OptionalConsumer<T> get(Class<T> type, Predicate<T> predicate) {
        return OptionalConsumer.of(getData(type).stream()
                .filter(predicate)
                .findFirst());
    }

    public <T extends DatabaseObject> T get(Class<T> type, Predicate<T> predicate, Supplier<T> ifNotPresent) {
        T object = getData(type).stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
        if (object == null) {
            object = ifNotPresent.get();
            getData().add(object);
        }

        return object;
    }

}
