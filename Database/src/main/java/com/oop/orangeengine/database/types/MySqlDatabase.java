package com.oop.orangeengine.database.types;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.oop.orangeengine.database.ODatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class MySqlDatabase extends ODatabase {

    private MySqlProperties properties;

    public MySqlDatabase(MySqlProperties props) {
        properties = props;
    }

    @Override
    protected Connection provideConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(properties.build(), properties.user(), properties.password);
//        conn.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + properties.database);
//        conn.createStatement().execute("USE " + properties.database + ";");
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
            return "jdbc:mysql://" + this.url + ":" + this.port + "/" + this.database;
        }
    }

    @Override
    public List<String> getTables() {
        Connection connection = getConnection();
        List<String> tables = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_type = 'base table'")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get tables", throwable);
        }
        return tables;
    }
}
