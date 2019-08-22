package com.oop.orangeengine.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public enum OColumn {

    INTEGER("INT"),
    FLOAT("FLOAT"),
    LONG("LONG"),
    TEXT("TEXT"),
    VARCHAR("VARCHAR(255)"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN");

    private String sql;

    OColumn(String sql) {
        this.sql = sql;
    }

    public static OColumn fromObject(Object obj) {

        if (obj instanceof Integer) return INTEGER;
        if (obj instanceof Float) return FLOAT;
        if (obj instanceof Double) return DOUBLE;
        if (obj instanceof Boolean) return BOOLEAN;
        if (obj instanceof String) return VARCHAR;

        return TEXT;

    }

    public static OColumn fromClass(Class klass) {

        if (klass == Integer.class || klass == int.class)
            return INTEGER;

        else if (klass == Boolean.class || klass == boolean.class)
            return BOOLEAN;

        else if (klass == Float.class || klass == float.class)
            return FLOAT;

        else if (klass == Double.class || klass == double.class)
            return DOUBLE;

        else if (klass == String.class)
            return VARCHAR;

        return TEXT;

    }

    public String getSql() {
        return sql;
    }

    public Object getObject(ResultSet rs) throws SQLException {
        if (this == INTEGER)
            return rs.getInt(1);

        else if (this == BOOLEAN)
            return rs.getBoolean(1);

        else if (this == FLOAT)
            return rs.getFloat(1);

        else if (this == DOUBLE)
            return rs.getDouble(1);

        else if(this == LONG)
            return rs.getLong(1);

        else if (this == VARCHAR || this == TEXT)
            return rs.getString(1);

        return rs.getObject(1);

    }
}
