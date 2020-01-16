package com.oop.orangeengine.database.holder;

import com.oop.orangeengine.database.object.DatabaseObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TestObject extends DatabaseObject {

    @PrimaryKey(name = "uuid")
    private UUID uuid;

    @Column(name = "objectNumba1")
    private DatabaseField<UUID> changeableUuid = new DatabaseField<>(UUID.class);

    public TestObject() {}

}
