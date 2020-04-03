package com.oop.testingPlugin;

import com.oop.orangeengine.database.types.MySqlDatabase;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import lombok.Getter;

@Getter
public class DatabaseController extends com.oop.orangeengine.database.DatabaseController {

    private PlayerHolder holder;
    public DatabaseController(TestingPlugin plugin) {
        setDatabase(new MySqlDatabase(
                new MySqlDatabase.MySqlProperties()
                        .database("customer_103989_test")
                        .user("customer_103989_test")
                        .password("W6OD~ZbtxNjPoIcB#TlT")
                        .url("eu01-sql.pebblehost.com")
        ));
        this.holder = new PlayerHolder(this);
        registerHolder(StatsPlayer.class, holder);

        plugin.getOLogger().printWarning(new MySqlDatabase(
                new MySqlDatabase.MySqlProperties()
                        .database("customer_103989_test")
                        .user("customer_103989_test")
                        .password("W6OD~ZbtxNjPoIcB#TlT")
                        .url("eu01-sql.pebblehost.com")
        ).getTables());
        load();
    }
}
