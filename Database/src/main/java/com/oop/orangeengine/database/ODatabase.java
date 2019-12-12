package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.AsyncQueue;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public abstract class ODatabase {

    private AsyncQueue queue = new AsyncQueue(this);

    protected abstract Connection provideConnection() throws SQLException, ClassNotFoundException;

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        return provideConnection();
    }

    public List<String> getColumns(String table) {
        List<String> columns = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, null)) {
                while (resultSet.next())
                    columns.add(resultSet.getString("COLUMN_NAME"));
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return columns;
    }

    public int getRowCount(String table) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) from " + table)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Integer> getRowIds(String table) {
        List<Integer> rowsIds = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM " + table)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next())
                        rowsIds.add(resultSet.getInt(1));
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rowsIds;
    }

    public boolean hasColumn(String table, String column) {
        return getColumns(table).contains(column);
    }

    public boolean tableHasValue(String table, String column, int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + column + " from " + table + " where id = '" + id + "'")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getObject(1) != null;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            new SQLException("Failed to check if object has value (table=" + table + ", column=" + column + ", id=" + id + ") cause of " + e.getMessage()).printStackTrace();
        }
        return false;
    }

    public void createColumn(String table, String column, OColumn columnType) {
        // Make sure the column doesn't exist so we don't get exception
        if (hasColumn(table, column)) return;

        execute("ALTER TABLE " + table + " ADD " + column + " " + columnType.getSql());
    }

    public void execute(String sql) {
        queue.add(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.execute();
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void executeNow(String sql) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.execute();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object gatherColumnValue(String table, DatabaseValue databaseValue, String identifierColumn, String identifierValue) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + databaseValue.columnName() + " from " + table + " where " + identifierColumn + "='" + identifierValue + "'")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return databaseValue.columnType().getObject(resultSet);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            new SQLException("Failed to gather column value (table=" + table + ", column=" + databaseValue.columnName() + ", " + identifierColumn + "=" + identifierValue + ") cause of " + e.getMessage()).printStackTrace();
        }
        return null;
    }

    public TableCreator newTableCreator() {
        return new TableCreator(this);
    }

    public TableEditor newTableEditor() {
        return new TableEditor(this);
    }

    public int insertAndGetId(String sql) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.execute();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next())
                    return rs.getInt(1);

                close(rs);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static class TableEditor {

        private String tableName;
        private ODatabase database;
        private List<OPair<String, OColumn>> columns = new LinkedList<>();

        public TableEditor(ODatabase database) {
            this.database = database;
        }

        public TableEditor setName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public TableEditor addColumn(String columnName, OColumn column) {
            this.columns.add(new OPair<>(columnName, column));
            return this;
        }

        public void edit() {
            try (Connection connection = database.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    for (OPair<String, OColumn> column : columns)
                        statement.executeUpdate("ALTER TABLE " + tableName + " ADD " + column.getFirst() + " " + column.getSecond().getSql());
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public class TableCreator {

        private ODatabase database;
        private String name;
        private List<OPair<String, OColumn>> columns = new LinkedList<>();

        private boolean primaryKey = false;

        private TableCreator(ODatabase database) {
            this.database = database;
        }

        public TableCreator setName(String name) {
            this.name = name;
            return this;
        }

        public TableCreator addColumn(String columnName, OColumn columnType) {
            columns.add(new OPair<>(columnName, columnType));
            return this;
        }

        public TableCreator addColumnPrimaryKey() {
            primaryKey = true;
            return this;
        }

        public ODatabase create() {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

            if (primaryKey) {
                if (database instanceof SqlLiteDatabase)
                    queryBuilder.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

                else
                    queryBuilder.append("id int AUTO_INCREMENT, ");

            }

            boolean first = true;
            for (OPair<String, OColumn> columnPair : columns) {
                if (first) {
                    queryBuilder.append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());
                    first = false;

                } else
                    queryBuilder.append(", ").append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());

            }

            if (primaryKey && !(database instanceof SqlLiteDatabase))
                queryBuilder.append(", PRIMARY KEY (id)");

            queryBuilder.append(")");
            database.executeNow(queryBuilder.toString());

            return database;
        }

        public void of(String name, List<OPair<Field, DatabaseValue>> fields) {
            setName(name);
            addColumnPrimaryKey();
            fields.forEach(field -> addColumn(field.getSecond().columnName(), field.getSecond().columnType()));
        }
    }

    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try (final Connection connection = getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null)) {
                while (resultSet.next())
                    tables.add(resultSet.getString(3));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tables;
    }

    private static void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
