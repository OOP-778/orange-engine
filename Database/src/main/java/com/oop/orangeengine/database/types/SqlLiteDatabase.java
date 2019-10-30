package com.oop.orangeengine.database.types;

import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.main.events.SyncEvents;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlLiteDatabase extends ODatabase {

    private String path;

    public SqlLiteDatabase(String path) {
        this.path = path;
        try (Connection conn = provideConnection()) {

        } catch (SQLException e){
            System.out.println("Failed to test the database!");
            e.printStackTrace();
        }
    }

    public SqlLiteDatabase(File folder, String name) {
        this("jdbc:sqlite:" + folder.getAbsolutePath() + File.separator + name + ".db");
    }

    @Override
    protected Connection provideConnection() throws SQLException {
        return DriverManager.getConnection(path);
    }
}
