package com.oop.orangeengine.database;

import com.oop.orangeengine.main.util.pair.OPair;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ODatabse {

    private Connection connection;

    abstract Connection provideConnection();

    public Connection getConnection() {

        try {
            if (connection.isClosed() || connection == null)
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
            connection.createStatement().executeQuery("ALTER TABLE " + table + " ADD " + column + " " + columnType.getSql());

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    public void execute(String statement) throws SQLException {
        getConnection().createStatement().executeQuery(statement);
    }

    public TableCreator newTableCreator() {
        return new TableCreator(this);
    }

    public class TableCreator {

        private ODatabse databse;
        private String name;
        private List<OPair<String, OColumn>> columns = new ArrayList<>();

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

        public ODatabse create() {

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("CREATE TABLE ").append(name).append(" (");

            boolean first = true;
            for(OPair<String, OColumn> columnPair : columns) {

                if(first) {
                    queryBuilder.append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());
                    first = false;

                } else
                    queryBuilder.append(", ").append(columnPair.getFirst()).append(" ").append(columnPair.getSecond().getSql());

            }

            queryBuilder.append(")");

            try {
                databse.execute(queryBuilder.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return databse;
        }

    }

}
