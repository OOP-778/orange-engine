package com.oop.orangeengine.database;

import com.oop.orangeengine.database.types.SQLiteDatabase;
import com.oop.orangeengine.main.util.pair.OPair;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ODatabse {

    private Connection connection;

    protected abstract Connection provideConnection();

    public Connection getConnection() {

        try {
            if (connection == null || connection.isClosed())
                connection = provideConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;

    }

    public List<String> getColumnNames(String table) {

        Connection conn = getConnection();
        DatabaseMetaData meta;
        List<String> columns = new ArrayList<>();

        try {

            meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, null);

            while (rs.next())
                columns.add(rs.getString("COLUMN_NAME"));

            conn.close();

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return columns;

    }

    public boolean hasColumn(String table, String column) {
        return getColumnNames(table).contains(column);
    }

    public void createColumn(String table, String column, OColumn columnType) {

        //Make sure the column doesn't exist so we don't get exception
        if (hasColumn(table, column)) return;

        try (Connection connection = getConnection()) {
            execute("ALTER TABLE " + table + " ADD " + column + " " + columnType.getSql(), connection);

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    public void execute(String statement, Connection connection) throws SQLException {
        connection.createStatement().execute(statement);
    }

    public Object gatherColumnValue(String table, String column, String identifierColumn, String identifierValue) throws SQLException {

        try (Connection connection = getConnection()) {

            ResultSet rs = connection.createStatement().executeQuery("SELECT " + column + " from " + table + " where " + identifierColumn + "='" + identifierValue + "'");
            rs.next();

            return rs.getObject(0);

        }
    }

    public TableCreator newTableCreator() {
        return new TableCreator(this);
    }

    public class TableCreator {

        private ODatabse databse;
        private String name;
        private List<OPair<String, OColumn>> columns = new ArrayList<>();

        private boolean primaryKey = false;

        private TableCreator(ODatabse databse) {
            this.databse = databse;
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

        public ODatabse create() {

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("CREATE TABLE ").append(name).append(" (");

            if (primaryKey) {

                if (databse instanceof SQLiteDatabase)
                    queryBuilder.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

                else
                    queryBuilder.append("id int AUTO_INCREMENT");

            }

            boolean first = true;
            for (OPair<String, OColumn> columnPair : columns) {

                if (first) {
                    queryBuilder.append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());
                    first = false;

                } else
                    queryBuilder.append(", ").append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());

            }

            if (primaryKey && !(databse instanceof SQLiteDatabase))
                queryBuilder.append(", PRIMARY KEY (id)");

            queryBuilder.append(")");
            System.out.println(queryBuilder);

            try {
                databse.execute(queryBuilder.toString(), getConnection());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return databse;
        }

    }

}
