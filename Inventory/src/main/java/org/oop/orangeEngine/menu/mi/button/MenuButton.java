package org.brian.core.mi.button;

import org.bukkit.inventory.ItemStack;

public class MenuButton extends AMenuButton {

    public MenuButton(ItemStack itemStack, int slot, String identifier) {
        super(itemStack, slot, identifier);
    }

    public MenuButton(ItemStack itemStack, int slot) {
        super(itemStack, slot, null);
    }

    public MenuButton(ItemStack itemStack) {
        super(itemStack, -1, null);
    }

}
