package com.oop.orangeengine.database.types;

import com.oop.orangeengine.database.ODatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlDatabase extends ODatabase {

    private MySqlProperties properties;

    public MySqlDatabase(MySqlProperties props) {
        properties = props;
    }

    @Override
    protected Connection provideConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(properties.build(), properties.user(), properties.password);
        conn.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + properties.database);
        conn.createStatement().execute("USE " + properties.database + ";");
        return conn;
    }

    @Accessors(fluent = true, chain = true)
    @Setter
    @Getter
    public static class MySqlProperties {

        String user = "root";
        String url = "localhost";
        String database = "orangeEngine";
        String password;
        int port = 3306;

        String build() {
            return "jdbc:mysql://" + url + ":" + port;
        }
    }

    @Override
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try (ResultSet resultSet = getConnection().getMetaData().getTables(getConnection().getCatalog(), "", null, new String[]{"TABLE"})) {
            while (resultSet.next())
                tables.add(resultSet.getString("TABLE_NAME"));
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get tables", throwable);
        }
        return tables;
    }
}
