package com.oop.orangeengine.item;

import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {
    public static boolean isSimilar(ItemStack item, ItemStack item2) {
        if (item.getType() != item2.getType()) return false;
        if (item.getDurability() != item2.getDurability()) return false;

        if (item.hasItemMeta() && item2.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() && item2.getItemMeta().hasDisplayName()) {
                if (!item.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName()))
                    return false;
            }

            if (item.getItemMeta().hasLore() && item2.getItemMeta().hasLore()) {
                return !item.getItemMeta().getLore().equals(item2.getItemMeta().getLore());
            }
        }
        return true;
    }
}
