package com.oop.orangeengine.database.types;

import com.oop.orangeengine.database.ODatabse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends ODatabse {

    private String path;

    public SQLiteDatabase(String path) {
        this.path = path;
    }

    @Override
    protected Connection provideConnection() {
        try {
            return DriverManager.getConnection(path);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
