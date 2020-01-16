package com.oop.orangeengine.database.holder;

import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface Saveable {

    default void save(DatabaseObject object, DatabaseHolder holder) {
        // Init database structure if found update
        Table table = object.getClass().getAnnotation(Table.class);
        if (table == null) {
            //TODO: Print out that the object doesn't have Table annotation
            return;
        }

        List<OPair<Field, Column>> fields = ClassUtil.getFields(object.getClass(), true);
        List<String> tablesRequired = new ArrayList<>();
        AtomicReference<PrimaryKey> key = new AtomicReference<>(null);

        for (OPair<Field, Column> field : fields) {
            if (key.get() == null)
                Optional.ofNullable(field.getFirst().getAnnotation(PrimaryKey.class)).ifPresent(key::set);
        }

    }

}
