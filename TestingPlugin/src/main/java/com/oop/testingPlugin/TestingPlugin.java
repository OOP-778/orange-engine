package com.oop.testingPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .serializeNulls()
                .create();


        DabClass dabClass = new DabClass(new OItem(Material.ANVIL).getItemStack());

        Instant now = Instant.now();
        print("Starting GSON");

        String serializedGson = gson.toJson(dabClass);
        print("Serializing Took: " + Duration.between(now, Instant.now()).toMillis());
        print(serializedGson);

        now = Instant.now();
        gson.fromJson(serializedGson, DabClass.class);
        print("Deserializing Took: " + Duration.between(now, Instant.now()).toMillis());

    }

    void print(Object ob) {
        Bukkit.getConsoleSender().sendMessage(ob.toString());
    }

    @Override
    public ITaskController provideTaskController() {
        return new ClassicTaskController(this);
    }
}

