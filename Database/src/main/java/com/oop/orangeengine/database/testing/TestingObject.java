package com.oop.orangeengine.database.testing;

import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "testing")
@Getter
@Setter
@AllArgsConstructor
public class TestingObject extends DatabaseObject {

    private TestingObject() {}

    @PrimaryKey(name = "uuid")
    private UUID uuid;

    @Column(name = "kills")
    private int kills;

}
