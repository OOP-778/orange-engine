package com.oop.orangeengine.database;

import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;

@DatabaseTable(tableName = "testingsTwo")
public class TestingObjectB extends TestingObject {

    @DatabaseValue(columnName = "oof2")
    private String oof2;


}
