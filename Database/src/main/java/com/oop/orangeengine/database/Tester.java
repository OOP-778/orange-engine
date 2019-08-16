package com.oop.orangeengine.database;

import com.oop.orangeengine.database.types.SQLiteDatabase;

public class Tester {

    public static void main(String[] args) {

        ODatabse databse = new SQLiteDatabase("jdbc:sqlite:identifier.sqlite");
        databse.newTableCreator()
                .setName("tes")
                .addColumnPrimaryKey()
                .addColumn("test", OColumn.BOOLEAN)
                .create();

        databse.getColumnNames("tes").forEach(System.out::println);

    }

}
