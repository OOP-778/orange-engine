package com.oop.testingPlugin.holder;

import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.database.newversion.DatabaseObject;
import com.oop.orangeengine.database.newversion.annotation.Column;
import com.oop.orangeengine.database.newversion.annotation.PrimaryKey;
import com.oop.orangeengine.database.newversion.annotation.Table;
import com.oop.orangeengine.database.newversion.suppliers.serializable.SerializableObject;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Table(name = "testObjects")
public class TestObject extends DatabaseObject {

    @Getter
    @PrimaryKey(name = "uuid")
    private UUID uuid;

    @Getter
    @Setter
    @Column(name = "obj1")
    private int yikes = ThreadLocalRandom.current().nextInt(151515);

    @Getter
    @Column(name = "inner")
    private ObjectInner inner = new ObjectInner();

    @Getter
    @Column(name = "auf")
    private int auuuf;

    @Getter
    @Column(name = "woof")
    private ItemStack itemStack;

    public TestObject() {
        _registerSupplier("auf", int.class, () -> 1);
        _registerSupplier("woof", ItemStack.class, () -> new OItem(OMaterial.HOPPER).setDisplayName("wagawgwa").addNBTTag("wagawg", "wgagwag").getItemStack());
    }

    @Getter
    public static class ObjectInner implements SerializableObject {

        private int id = ThreadLocalRandom.current().nextInt(252525);

        @SerializedName(value = "woof")
        private String woof;

        public ObjectInner() {
            _registerSupplier("woof", String.class, () -> "WOOOOOOOOOF!");
        }

    }

}
