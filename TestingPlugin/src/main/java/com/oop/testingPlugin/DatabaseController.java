package com.oop.testingPlugin;

import com.oop.orangeengine.database.types.SqlLiteDatabase;
import lombok.Getter;

@Getter
public class DatabaseController extends com.oop.orangeengine.database.DatabaseController {

    private PlayerHolder holder;
    public DatabaseController(TestingPlugin plugin) {
        setDatabase(new SqlLiteDatabase(plugin.getDataFolder(), "database"));
        this.holder = new PlayerHolder(this);
        registerHolder(StatsPlayer.class, holder);

        load();
    }
}
