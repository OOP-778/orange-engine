package com.oop.orangeengine.database.testing;

import com.oop.orangeengine.database.ODatabase;

public class DatabaseController extends com.oop.orangeengine.database.DatabaseController {

    public DatabaseController(ODatabase database) {
        setDatabase(database);

        TestingHolder holder = new TestingHolder(this);
        registerHolder(TestingObject.class, holder);

        load();
    }
}
