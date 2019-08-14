package com.oop.orangeengine.item.custom;

import org.bukkit.inventory.ItemStack;

public class OBanner extends OItem {

    public OBanner(ItemStack item) {
        super(item);
    }

    @Override
    public String getType() {
        return "banner";
    }
}
