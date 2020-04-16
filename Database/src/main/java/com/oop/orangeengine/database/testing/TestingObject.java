package com.oop.orangeengine.database.testing;

import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.database.suppliers.serializable.SerializableObject;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class TestingObject implements SerializableObject {

    @SerializedName(value = "id")
    private String id = ThreadLocalRandom.current().nextInt(555) + "";

    @SerializedName(value = "uuid")
    private UUID uuid = UUID.randomUUID();

    @SerializedName(value = "random")
    private int random = ThreadLocalRandom.current().nextInt(25);

    @SerializedName(value = "child")
    private TestingChild child = new TestingChild();

    @SerializedName(value = "testEnum")
    private TestEnum testEnum = TestEnum.HElLO_2;

    TestingObject() {}

    public static class TestingChild {
        @SerializedName(value = "hello")
        private int id2 = ThreadLocalRandom.current().nextInt(2525);

        @SerializedName(value = "uuid")
        private UUID uuid = UUID.randomUUID();

        @SerializedName(value = "testEnum")
        private TestEnum testEnum = TestEnum.HELLO;
    }
}
