package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonItemSwapEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class SwappableButton extends AMenuButton {

    private ItemStack toSwap;
    private int orgHashCode = -1;

    public SwappableButton(ItemStack item, int slot) {
        super(item, slot);
    }

    public SwappableButton(ItemStack item) {
        super(item, -1);
    }

    public void swap() {
        if (toSwap == null) return;

        ItemStack swapWith = currentItem().clone();
        ButtonItemSwapEvent swapEvent = new ButtonItemSwapEvent(toSwap, swapWith);
        Bukkit.getPluginManager().callEvent(swapEvent);

        if (orgHashCode == -1)
            orgHashCode = swapWith.hashCode();

        currentItem(toSwap);
        toSwap = swapWith;
    }

    public boolean isSwapped() {
        return orgHashCode != -1 || toSwap.hashCode() != currentItem().hashCode();
    }

}
