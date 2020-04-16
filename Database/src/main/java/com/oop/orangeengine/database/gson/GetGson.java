package com.oop.orangeengine.database.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.database.gson.legacy.MapFactory;
import com.oop.orangeengine.database.gson.legacy.RuntimeClassFactory;
import com.oop.orangeengine.main.gson.ItemStackAdapter;
import org.bukkit.inventory.ItemStack;

public class GetGson {
    private static Gson gson;

    static {
        useLegacy();
    }

    private static void useLegacy() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(new MapFactory())
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapterFactory(RuntimeClassFactory.of(Object.class))
                .create();
    }

    public static void useNew() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(new com.oop.orangeengine.database.gson.MapFactory())
                .registerTypeAdapterFactory(new CollectionFactory())
                .registerTypeAdapterFactory(ObjectFactory.FACTORY)
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .create();
    }

    public static Gson get() {
        return gson;
    }
}
