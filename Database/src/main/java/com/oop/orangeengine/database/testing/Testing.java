package com.oop.orangeengine.database.testing;

import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.types.SqlLiteDatabase;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class Testing {
    public static void main(String[] args) {
        ODatabase database = new SqlLiteDatabase(new File("D:\\Work\\orangeengine\\Database"), "data");
        DatabaseController controller = new DatabaseController(database);

        TestingHolder holder = controller.holder(TestingObject.class).map(holder2 -> (TestingHolder) holder2).get();
        TestingObject object = new TestingObject(UUID.randomUUID(), 0);
        holder.add(object);
    }
}
