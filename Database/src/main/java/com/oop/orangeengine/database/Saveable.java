package com.oop.orangeengine.database;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import com.oop.orangeengine.database.handler.DataHandler;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import com.oop.orangeengine.database.util.ClassUtil;
import com.oop.orangeengine.database.util.DefaultValues;
import com.oop.orangeengine.database.util.OColumn;
import com.oop.orangeengine.database.util.ObjectState;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.oop.orangeengine.database.util.Tester.t;
import static com.oop.orangeengine.database.util.Tester.twr;
import static com.oop.orangeengine.main.Engine.getEngine;

public interface Saveable {

    default void remove(Collection<DatabaseObject> objs, @NonNull DatabaseController databaseController) {
        Map<Class<? extends DatabaseObject>, OPair<TableStructure, DatabaseHolder<?, ? extends DatabaseObject>>> structureMap = Maps.newHashMap();

        for (DatabaseObject object : objs) {
            OPair<TableStructure, DatabaseHolder<?, ? extends DatabaseObject>> info = structureMap.computeIfAbsent(object.getClass(), clazz -> {
                TableStructure structure = structureOf(clazz);
                DatabaseHolder<?, ? extends DatabaseObject> holder = databaseController
                        .holder(clazz)
                        .orElse(null);

                return new OPair<>(
                        Objects.requireNonNull(structure, "Failed to initialize table structure for " + clazz.getSimpleName()),
                        Objects.requireNonNull(holder, "Failed to get holder for " + clazz.getSimpleName())
                );
            });

            // get primary key as string
            try {
                String primaryKeyString = Wrappers.wrap(null, info.getFirst().getPrimaryKey(), object);
                if (!primaryKeyString.contentEquals("null") && twr("Primary Key check", () -> databaseController.getDatabase().tableHasPKUsed(info.getFirst().getTable().name(), info.getFirst().getPrimaryKey().getSecond(), primaryKeyString))) {
                    databaseController.getDatabase().execute("DELETE FROM " + info.getFirst().getTable().name() + " WHERE " + info.getFirst().getPrimaryKey().getSecond().name() + " = " + primaryKeyString);

                    // Check to make sure
                    boolean bool = databaseController.getDatabase().tableHasPKUsed(info.getFirst().getTable().name(), info.getFirst().getPrimaryKey().getSecond(), primaryKeyString);
                    getEngine().getLogger().printDebug("Successfully deleted object from database? " + bool);

                }
            } catch (Throwable thrw) {
                thrw.printStackTrace();
            }
        }
    }

    default void save(Collection<DatabaseObject> objs, @NonNull DatabaseController databaseController) {
        Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> mappedObjects = Maps.newHashMap();
        for (DatabaseObject obj : objs)
            mappedObjects.computeIfAbsent(obj.getClass(), (clazz) -> new HashSet<>()).add(obj);

        Map<Class<? extends DatabaseObject>, TableStructure> structureMap = Maps.newConcurrentMap();
        Map<Class<? extends DatabaseObject>, DatabaseHolder<?, ? extends DatabaseObject>> holderMap = Maps.newConcurrentMap();

        mappedObjects.values().parallelStream().forEach(grouped -> grouped.forEach(object -> {
            DatabaseHolder<?, ? extends DatabaseObject> holder = holderMap.computeIfAbsent(object.getClass(), (clazz) -> {
                Optional<? extends DatabaseHolder<?, ? extends DatabaseObject>> databaseHolder = databaseController.holder(clazz);
                if (!databaseHolder.isPresent())
                    throw new IllegalStateException("Failed to save object " + object.getClass().getSimpleName() + " because database holder is not found!");

                return databaseHolder.get();
            });

            TableStructure structure = structureMap.computeIfAbsent(object.getClass(), clazz -> {
                TableStructure info = twr("Structure of ", () -> structureOf(clazz));
                if (databaseController.getDatabase().getTables().contains(info.getTable().name()))
                    t("Update structure", () -> updateStructure(info, databaseController.getDatabase()));

                else
                    t("Insert structure", () -> insertStructure(info, databaseController.getDatabase()));

                return info;
            });

            try {
                String primaryKeyString = Wrappers.wrap(null, structure.getPrimaryKey(), object);
                if (primaryKeyString.contentEquals("null") || !twr("Primary Key check", () -> databaseController.getDatabase().tableHasPKUsed(structure.getTable().name(), structure.getPrimaryKey().getSecond(), primaryKeyString))) {
                    t("Insert Object ", () -> {
                        try {
                            insertObject(object, structure, holder);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

                } else
                    updateObject(object, structure, holder);

                ((DatabaseHolder<?, DatabaseObject>) holder).onSave(object);
            } catch (Throwable thrw) {
                thrw.printStackTrace();
            }
        }));
    }

    default <T extends DatabaseObject> void save(@NonNull T object, @NonNull DatabaseController databaseController) {
        Optional<DatabaseHolder<?, T>> holder = databaseController.holder(object.getClass()).map(holder2 -> (DatabaseHolder<?, T>) holder2);
        if (!holder.isPresent())
            throw new IllegalStateException("Failed to save object " + object.getClass().getSimpleName() + " because database holder is not found!");

        save(object, holder.get());
    }

    default <T extends DatabaseObject> void save(@NonNull T object, @NonNull DatabaseHolder<?, T> holder) {
        object._loadSupplier();

        DatabaseController databaseController = holder.getDatabaseController();
        TableStructure info = structureOf(object.getClass());
        if (databaseController.getDatabase().getTables().contains(info.getTable().name()))
            updateStructure(info, databaseController.getDatabase());

        else
            insertStructure(info, databaseController.getDatabase());

        List<String> tableColumns = databaseController.getDatabase().getColumns(info.getTable().name());
        info.getColumns().sort(Comparator.comparing(obj -> tableColumns.indexOf(obj.getSecond().name())));

        try {
            Object primaryKeyObject = info.getPrimaryKey().getFirst().get(object);
            if (primaryKeyObject == null || !databaseController.getDatabase().tableHasPKUsed(info.getTable().name(), info.getPrimaryKey().getSecond(), Wrappers.wrap(null, info.getPrimaryKey(), object))) {
                insertObject(object, info, holder);

            } else
                updateObject(object, info, holder);

            holder.onSave(object);
        } catch (Throwable thrw) {
            thrw.printStackTrace();
        }
    }

    default void updateObject(@NonNull DatabaseObject object, @NonNull TableStructure info, @NonNull DatabaseHolder databaseHolder) {
        StringBuilder updateBuilder = new StringBuilder()
                .append("UPDATE ")
                .append(info.getTable().name())
                .append(" SET ");

        List<OPair<Field, Column>> columns = new LinkedList<>(info.getColumns());
        Map<String, Integer> hashCodes = object.getHashCodes();

        columns.removeIf(pair -> {
            try {
                Object fieldObject = pair.getFirst().get(object);
                if (fieldObject instanceof DatabaseField) {
                    return !((DatabaseField) fieldObject).requiresUpdate();

                } else {
                    Integer hashCode = hashCodes.get(pair.getSecond().name());
                    if (hashCode == null)
                        hashCode = -1;

                    int currentHashCode = fieldObject.hashCode();
                    if (currentHashCode != hashCode) {
                        hashCodes.remove(pair.getSecond().name());
                        hashCodes.put(pair.getSecond().name(), currentHashCode);
                    }

                    return currentHashCode == hashCode;
                }

            } catch (Throwable thrw) {
                return true;
            }
        });
        if (columns.isEmpty()) return;

        boolean first = true;
        for (OPair<Field, Column> column : columns) {
            if (first) {
                updateBuilder.append(column.getSecond().name()).append(" = ?");
                first = false;

            } else
                updateBuilder.append(", ").append(column.getSecond().name()).append(" = ?");
        }

        updateBuilder.append(" WHERE ").append(info.getPrimaryKey().getSecond().name()).append(" = ?");

        try (PreparedStatement statement = databaseHolder.getDatabaseController().getDatabase().getConnection().prepareStatement(updateBuilder.toString())) {
            Set<Integer> indexes = Sets.newHashSet();
            IntStream.range(1, columns.size() + 1).forEach(index -> {

                String wrapped;
                indexes.add(index);
                try {
                    wrapped = Wrappers.wrap(columns.get(index - 1), info.getPrimaryKey(), object);
                } catch (Throwable thrw) {
                    throw new IllegalStateException("Failed to serialize field of " + object.getClass().getSimpleName() + " at " + columns.get(index - 1).getSecond().name() + ", cause: ", thrw);
                }
                try {
                    getEngine().getLogger().printDebug(Wrappers.wrap(null, info.getPrimaryKey(), object) + "Stat index: " + index + ", obj: " + wrapped);
                    statement.setString(index, wrapped);
                } catch (Throwable thrw) {
                    throw new IllegalStateException("Failed to set statement's value of index " + index + " for object " + object.getClass().getSimpleName() + " column " + columns.get(index - 1).getSecond().name() + ", cause: ", thrw);
                }
            });

            statement.setString(indexes.stream().max(Comparator.naturalOrder()).orElse(0) + 1, Wrappers.wrap(null, info.getPrimaryKey(), object));
            statement.executeUpdate();

        } catch (Throwable thrw) {
            thrw.printStackTrace();
        }
    }

    /**
     * Inserts database object into the database
     *
     * @param object object that needs to be inserted
     * @param info   table information containing object
     * @param holder holder that holds the object's classes
     * @throws IllegalAccessException if it fails to insert, it will throw the exception
     */
    default void insertObject(@NonNull DatabaseObject object, @NonNull TableStructure info, @NonNull DatabaseHolder holder) throws IllegalAccessException {
        // Generate primary key for the object
        if (info.getPrimaryKey().getFirst().get(object) == null)
            info.getPrimaryKey().getFirst().set(object, holder.generatePrimaryKey(object));

        StringBuilder insertBuilder = new StringBuilder()
                .append("INSERT INTO ")
                .append(info.getTable().name())
                .append(" (")
                .append(info.getPrimaryKey().getSecond().name());

        for (OPair<Field, Column> column : info.getColumns())
            insertBuilder.append(", ").append(column.getSecond().name());

        insertBuilder.append(") VALUES (?");
        for (OPair<Field, Column> column : info.getColumns())
            insertBuilder.append(", ?");

        insertBuilder.append(")");

        try (PreparedStatement statement = holder.getDatabaseController().getDatabase().getConnection().prepareStatement(insertBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {
            String primaryWrapped;

            try {
                primaryWrapped = Wrappers.wrap(null, info.getPrimaryKey(), object);
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to serialize primary key of " + object.getClass().getSimpleName() + " at " + info.getPrimaryKey().getSecond().name() + ", cause: ", thrw);
            }

            try {
                statement.setString(1, primaryWrapped);
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to set statement's primary key for object " + object.getClass().getSimpleName() + " column " + info.getPrimaryKey().getSecond().name() + ", cause: ", e);
            }

            IntStream.range(2, info.getColumns().size() + 2).forEach(index -> {
                String wrapped;
                try {
                    wrapped = Wrappers.wrap(info.getColumns().get(index - 2), info.getPrimaryKey(), object);
                } catch (Throwable thrw) {
                    throw new IllegalStateException("Failed to serialize field of " + object.getClass().getSimpleName() + " at " + info.getColumns().get(index - 2).getSecond().name() + ", cause: ", thrw);
                }
                try {
                    statement.setString(index, wrapped);
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to set statement's value of index " + index + " for object " + object.getClass().getSimpleName() + " column " + info.getColumns().get(index - 2).getSecond().name() + ", cause: ", e);
                }
            });

            t("object insert", () -> {
                try {
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to insert object " + object.getClass().getSimpleName() + " with id of: " + info.getPrimaryKey().getFirst().get(object) + " cause: ", thrw);
        }
    }

    default TableStructure structureOf(Class<? extends DatabaseObject> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null)
            throw new IllegalStateException("Failed to make table structure out of " + clazz.getSimpleName() + " cause of it's missing Table annotation!");

        List<OPair<Field, Column>> columns = new ArrayList<>();
        OPair<Field, PrimaryKey> primaryKey = new OPair<>(null, null);

        for (Field field : ClassUtil.getFields(clazz)) {

            Column column = field.getAnnotation(Column.class);
            PrimaryKey primKey = field.getAnnotation(PrimaryKey.class);

            if (primaryKey.getFirst() == null && primKey != null)
                primaryKey.setFirst(field).setSecond(primKey);

            else if (column != null)
                columns.add(new OPair<>(field, column));
        }
        columns.sort(Comparator.comparing(pair -> pair.getSecond().name()));
        return new TableStructure(table, columns, primaryKey);
    }

    /**
     * Updates structure of the database table (can remove & add columns, add primary keys)
     *
     * @param structure Generated object holding all the data needed for updating the structure
     * @param database  Database object
     */
    default void updateStructure(@NonNull TableStructure structure, @NonNull ODatabase database) {
        List<String> tableColumns = database.getColumns(structure.getTable().name());
        List<String> structColumns = structure.getColumns()
                .stream()
                .map(struct -> struct.getSecond().name())
                .collect(Collectors.toList());

        if (tableColumns.containsAll(structColumns) && structColumns.containsAll(tableColumns)) return;
        TableEditor editor = new TableEditor(structure.getTable());

        // For dropping columns
        for (String tableColumn : tableColumns) {
            if (structColumns.contains(tableColumn) || structure.getPrimaryKey().getSecond().name().contentEquals(tableColumn))
                continue;

            editor.removeColumn(tableColumn);
        }

        // For adding columns
        structColumns
                .stream()
                .filter(column -> !tableColumns.contains(column))
                .map(column -> structure.getColumns().stream().filter(column2 -> column2.getSecond().name().contentEquals(column)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .forEach(column -> editor.addColumn(column.getSecond().name(), OColumn.from(column.getFirst().getGenericType()).getSql()));

        // For adding primary keys
        if (!database.getPrimaryKeys(structure.getTable().name()).contains(structure.getPrimaryKey().getSecond().name()))
            editor.addPrimaryKey(structure.getPrimaryKey().getSecond().name());

        editor.edit(database);
    }

    default void insertStructure(@NonNull TableStructure structure, @NonNull ODatabase database) {
        StringBuilder insertBuilder = new StringBuilder("CREATE TABLE " + structure.getTable().name() + " (");

        if (database instanceof SqlLiteDatabase) {
            insertBuilder.append(structure.getPrimaryKey().getSecond().name()).append(" ").append(OColumn.from(structure.getPrimaryKey().getFirst().getGenericType()).getSql()).append(" PRIMARY KEY, ");

        } else
            insertBuilder.append(structure.getPrimaryKey().getSecond().name()).append(" VARCHAR(255)").append(", ");

        boolean first = true;
        for (OPair<Field, Column> column : structure.getColumns()) {
            if (first) {
                insertBuilder.append(column.getSecond().name()).append(" ").append(OColumn.from(column.getFirst().getGenericType()).getSql());
                first = false;

            } else
                insertBuilder.append(", ").append(column.getSecond().name()).append(" ").append(OColumn.from(column.getFirst().getGenericType()).getSql());
        }

        if (!(database instanceof SqlLiteDatabase))
            insertBuilder.append(", PRIMARY KEY (").append(structure.getPrimaryKey().getSecond().name()).append(")");

        insertBuilder.append(")");
        database.execute(insertBuilder.toString());
    }

    @Getter
    @AllArgsConstructor
    class TableStructure {

        private Table table;
        private List<OPair<Field, Column>> columns;

        private OPair<Field, PrimaryKey> primaryKey;

    }

    @RequiredArgsConstructor
    class TableEditor {

        private final Table table;
        private List<OPair<String, String>> addColumns = new ArrayList<>();
        private List<String> removeColumns = new ArrayList<>();
        private List<String> addPrimaryKeys = new ArrayList<>();

        public void addColumn(String name, String type) {
            addColumns.add(new OPair<>(name, type));
        }

        public void removeColumn(String name) {
            removeColumns.add(name);
        }

        public void edit(ODatabase database) {
            try (Statement stmt = database.getConnection().createStatement()) {

                // Add columns
                for (OPair<String, String> column : addColumns)
                    stmt.executeUpdate("ALTER TABLE " + table.name() + " ADD " + column.getFirst() + " " + column.getSecond());

                // Remove columns
                for (String column : removeColumns) {
                    stmt.executeUpdate("ALTER TABLE " + table.name() + " DROP " + column);
                }

                // Add primary keys
                for (String primaryKey : addPrimaryKeys) {
                    stmt.executeUpdate("ALTER TABLE " + table.name() + " ADD CONSTRAINT " + primaryKey + " PRIMARY KEY CLUSTERED (" + primaryKey + ")");
                }
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to edit " + table.name() + " cause " + thrw.getMessage(), thrw);
            }
        }

        public void addPrimaryKey(String name) {
            addPrimaryKeys.add(name);
        }
    }

    class Wrappers {
        static String wrap(OPair<Field, Column> pair, OPair<Field, PrimaryKey> primKey, DatabaseObject databaseObject) throws IllegalStateException, IllegalAccessException {
            Object obj = pair != null ? pair.getFirst().get(databaseObject) : primKey.getFirst().get(databaseObject);
            if (obj == null)
                return "null";

            DataHandler<Object> dataHandler = (DataHandler<Object>) databaseObject.dataHandlerFor(pair != null ? pair.getSecond().name() : primKey.getSecond().name());
            String wrapped;
            try {
                wrapped = dataHandler.serialize(pair != null ? pair.getFirst() : primKey.getFirst(), obj);
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to serialize object of type " + obj.getClass() + " of column " + (pair != null ? pair.getSecond().name() : primKey.getSecond().name()) + " for id: " + primKey.getFirst().get(databaseObject));
            }

            return wrapped;
        }

        static Object unwrap(String wrapped, OPair<Field, Column> pair, OPair<Field, PrimaryKey> primKey, DatabaseObject databaseObject) throws IllegalAccessException {
            Class<?> objType = pair != null ? pair.getFirst().getType() : primKey.getFirst().getType();
            if (wrapped == null || wrapped.equalsIgnoreCase("null")) {
                if (objType.isAssignableFrom(Number.class)) {
                    return -1;

                } else {
                    return DefaultValues.forClass(objType);
                }
            }

            DataHandler<Object> dataHandler = (DataHandler<Object>) databaseObject.dataHandlerFor(pair != null ? pair.getSecond().name() : primKey.getSecond().name());
            Object unwrapped;
            try {
                unwrapped = dataHandler.deserialize(pair != null ? pair.getFirst() : primKey.getFirst(), wrapped);
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to deserialize object of type " + (pair != null ? pair.getFirst() : primKey.getFirst()).getGenericType() + " of column " + (pair != null ? pair.getSecond().name() : primKey.getSecond().name()) + " for id: " + primKey.getFirst().get(databaseObject), thrw);
            }
            return unwrapped;
        }
    }

    default void load(DatabaseHolder<?, ?> holder) {
        ODatabase database = holder.getDatabaseController().getDatabase();
        holder.getObjectVariants().parallelStream().forEach(tableClazz -> {
            TableStructure info = structureOf(tableClazz);
            if (!database.getTables().contains(info.getTable().name()))
                return;

            Constructor<DatabaseObject> constructor = null;
            try {
                constructor = (Constructor<DatabaseObject>) tableClazz.getDeclaredConstructor();
                constructor.setAccessible(true);
            } catch (Throwable thrw) {
                throw new IllegalStateException("Failed to gather default constructor for " + tableClazz.getSimpleName() + " cause of " + thrw.getMessage(), thrw);
            }

            List<String> rowIds = database.getRowIds(info.getPrimaryKey().getSecond(), info.getTable());
            Constructor<DatabaseObject> finalConstructor = constructor;

            rowIds.parallelStream().forEach(primaryKey -> {
                DatabaseObject obj = null;
                try {
                    obj = finalConstructor.newInstance();
                } catch (Throwable thrw) {
                    throw new IllegalStateException("Failed to construct object for " + tableClazz.getSimpleName() + " cause of " + thrw.getMessage(), thrw);
                }

                try {
                    // Set primary key
                    info.getPrimaryKey().getFirst().set(obj, Wrappers.unwrap(primaryKey, null, info.getPrimaryKey(), obj));
                    DatabaseObject finalObj = obj;

                    List<String> columns = database.getColumns(info.getTable().name());
                    info.getColumns().forEach(column -> {
                        if (!columns.contains(column.getSecond().name())) return;
                        String stringValue = database.gatherColumnValue(info.getTable().name(), column.getValue().name(), info.getPrimaryKey().getSecond().name(), primaryKey);

                        try {
                            if (column.getFirst().getType().isAssignableFrom(DatabaseField.class)) {
                                DatabaseField field = (DatabaseField) column.getFirst().get(finalObj);
                                Object unwrappedObject = Wrappers.unwrap(stringValue, column, info.getPrimaryKey(), finalObj);
                                field.set(unwrappedObject, false);

                                if (unwrappedObject != null)
                                    finalObj.getHashCodes().put(column.getSecond().name(), unwrappedObject.hashCode());

                            } else {
                                Object unwrappedObject = Wrappers.unwrap(stringValue, column, info.getPrimaryKey(), finalObj);
                                column.getFirst().set(finalObj, unwrappedObject);

                                if (unwrappedObject != null)
                                    finalObj.getHashCodes().put(column.getSecond().name(), unwrappedObject.hashCode());
                            }
                        } catch (Throwable thrw) {
                            throw new IllegalStateException("Failed to set field value of column " + column.getSecond().name(), thrw);
                        }
                    });

                    ((DatabaseHolder<?, DatabaseObject>) holder).add(obj, false);
                    obj.setHolder(holder);
                    obj.setObjectState(ObjectState.LOADED);
                    obj._loadSupplier();
                    obj.onLoad();

                    obj.save(true);
                } catch (Throwable thrw) {
                    throw new IllegalStateException("Failed to load object with primary key of " + primaryKey + " cause of " + thrw.getMessage(), thrw);
                }
            });
        });
    }
}
