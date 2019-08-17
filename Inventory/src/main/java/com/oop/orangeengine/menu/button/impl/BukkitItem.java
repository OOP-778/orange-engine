package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.menu.button.AMenuButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BukkitItem extends AMenuButton {

    public BukkitItem(ItemStack currentItem, int slot) {
        super(currentItem, slot);
    }

    public static BukkitItem newAir(int slot) {
        return new BukkitItem(new ItemStack(Material.AIR), slot);
    }

}
