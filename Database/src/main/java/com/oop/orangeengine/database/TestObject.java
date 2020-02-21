package com.oop.orangeengine.database;
import com.oop.orangeengine.database.newversion.*;
import com.oop.orangeengine.database.newversion.annotation.Column;
import com.oop.orangeengine.database.newversion.annotation.PrimaryKey;
import com.oop.orangeengine.database.newversion.annotation.Table;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Table(name = "testObjects")
@Getter
public class TestObject extends DatabaseObject {

    @PrimaryKey(name = "uuid")
    public UUID uuid;

    @Column(name = "count")
    public DatabaseField<Integer> count = new DatabaseField<>(Integer.class);

    @Column(name = "inner")
    public InnerObject innerObject = new InnerObject();

    @Column(name = "yikes")
    public int yikes = 2525;

    public TestObject(int count) {
        this.count.set(count);
    }

    protected TestObject() {}

    public static class InnerObject {

        public int randomId = ThreadLocalRandom.current().nextInt(999);

        public int randomId2 = ThreadLocalRandom.current().nextInt(999);

        public int randomId3 = ThreadLocalRandom.current().nextInt(999);

        public int randomId4 = ThreadLocalRandom.current().nextInt(999);
    }

    @Table(name = "innerObjects")
    public static class InnerClass extends TestObject {

        @PrimaryKey(name = "uuid1")
        public UUID uuid;

        @Column(name = "randm")
        private int randomId5 = ThreadLocalRandom.current().nextInt(999);

        public InnerClass() {
            super(2);
        }
    }

}
