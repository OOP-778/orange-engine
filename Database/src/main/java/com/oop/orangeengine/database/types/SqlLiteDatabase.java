package com.oop.orangeengine.database.types;

import com.oop.orangeengine.database.ODatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlLiteDatabase extends ODatabase {

    private String path;

    public SqlLiteDatabase(String path) {
        this.path = path;
    }

    public SqlLiteDatabase(File folder, String name) {
        this("jdbc:sqlite:" + folder.getAbsolutePath() + File.separator + name + ".db");
    }

    @Override
    protected Connection provideConnection() throws SQLException {
        return DriverManager.getConnection(path);
    }
}