package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.newversion.annotation.PrimaryKey;
import com.oop.orangeengine.database.newversion.annotation.Table;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public abstract class ODatabase {

    protected abstract Connection provideConnection() throws SQLException, ClassNotFoundException;

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = provideConnection();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }

    private Connection connection;

    public List<String> getColumns(String table) {
        List<String> columns = new ArrayList<>();

        try {
            try (ResultSet resultSet = getConnection().getMetaData().getColumns(null, null, table, null)) {
                while (resultSet.next())
                    columns.add(resultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return columns;
    }

    public int getRowCount(String table) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT COUNT(*) from " + table)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Integer> getRowIds(String table) {
        List<Integer> rowsIds = new ArrayList<>();

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT id FROM " + table)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    rowsIds.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsIds;
    }

    public List<String> getRowIds(PrimaryKey pk, Table table) {
        List<String> rowsIds = new ArrayList<>();

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + pk.name() + " FROM " + table.name())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    rowsIds.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsIds;
    }

    public boolean hasColumn(String table, String column) {
        return getColumns(table).contains(column);
    }

    public boolean tableHasPKUsed(String table, PrimaryKey pki, String primaryKey) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + pki.name() + " from " + table + " where " + pki.name() + " = '" + primaryKey + "'")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getObject(1) != null;
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to check if table contains value (table=" + table + ", " + pki.name() + "=" + primaryKey + ") cause of " + e.getMessage(), e);
        }
    }

    public void execute(String sql) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeNow(String sql) {
        execute(sql);
    }

    public Object gatherColumnValue(String table, DatabaseValue databaseValue, String identifierColumn, String identifierValue) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + databaseValue.columnName() + " from " + table + " where " + identifierColumn + "='" + identifierValue + "'")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return databaseValue.columnType().getObject(resultSet);
            }
        } catch (SQLException  e) {
            new SQLException("Failed to gather column value (table=" + table + ", column=" + databaseValue.columnName() + ", " + identifierColumn + "=" + identifierValue + ") cause of " + e.getMessage()).printStackTrace();
        }
        return null;
    }

    public String gatherColumnValue(String table, String column, String identifierColumn, String identifierValue) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + column + " from " + table + " where " + identifierColumn + "='" + identifierValue + "'")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getString(1);
            }
        } catch (SQLException  e) {
            new SQLException("Failed to gather column value (table=" + table + ", column=" + column + ", " + identifierColumn + "=" + identifierValue + ") cause of " + e.getMessage()).printStackTrace();
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
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);

            close(rs);
        } catch (SQLException e) {
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
            try (Statement statement = database.getConnection().createStatement()) {
                for (OPair<String, OColumn> column : columns)
                    statement.executeUpdate("ALTER TABLE " + tableName + " ADD " + column.getFirst() + " " + column.getSecond().getSql());
            } catch (SQLException e) {
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
        try (ResultSet resultSet = getConnection().getMetaData().getTables(null, null, null, null)) {
            while (resultSet.next())
                tables.add(resultSet.getString(3));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<String> getPrimaryKeys(@NonNull String tableName) {
       Connection connection = getConnection();
       List<String> primaryKeys = new ArrayList<>();

       try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, null, tableName)) {
           while (rs.next())
               primaryKeys.add(rs.getString("COLUMN_NAME"));

       } catch (Throwable thrw) {
           throw new IllegalStateException("Failed to get primary keys of table " + tableName + " cause " + thrw.getMessage(), thrw);
       }
       return primaryKeys;
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
