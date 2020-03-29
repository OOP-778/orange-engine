package com.oop.testingPlugin;

import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Table(name = "stats")
public class StatsPlayer extends DatabaseObject {

    @PrimaryKey(name = "uuid")
    private final UUID uuid;

    @Column(name = "kills")
    @Setter
    private int kills = 0;

    public StatsPlayer(UUID uuid) {
        this.uuid = uuid;
    }
}
