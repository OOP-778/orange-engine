package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.menu.button.AMenuButton;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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

    public void swap() {
        ItemStack swapWith = currentItem().clone();
        if (orgHashCode == -1)
            orgHashCode = swapWith.hashCode();

        currentItem(toSwap);

        toSwap = swapWith;
    }

    public boolean isSwapped() {
        return orgHashCode != -1 || toSwap.hashCode() != currentItem().hashCode();
    }

}
