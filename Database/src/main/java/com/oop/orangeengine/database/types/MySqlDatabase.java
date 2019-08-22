package com.oop.orangeengine.database.types;

import com.oop.orangeengine.database.ODatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

}
