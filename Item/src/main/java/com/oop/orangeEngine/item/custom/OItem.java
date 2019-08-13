package com.oop.orangeEngine.item.custom;

import com.oop.orangeEngine.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;

public class OItem extends ItemBuilder {

    public OItem(ItemStack item) {
        super(item);
    }

    @Override
    public String getType() {
        return null;
    }
}
