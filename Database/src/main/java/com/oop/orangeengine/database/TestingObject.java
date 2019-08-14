package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;

@DatabaseTable(tableName = "testing")
public abstract class TestingObject implements DatabaseObject {

    @DatabaseValue(columnName = "oof", rowType = OColumn.VARCHAR)
    private String oof;

    public TestingObject() {

    }

}
