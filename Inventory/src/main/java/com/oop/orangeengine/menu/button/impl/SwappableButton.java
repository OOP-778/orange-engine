package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.menu.button.AMenuButton;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class SwappableButton extends AMenuButton {

    private ItemStack toSwap;
    public SwappableButton() {}

    public SwappableButton swap() {

        ItemStack swapWith = getCurrentItem().clone();
        setCurrentItem(toSwap);
        toSwap = swapWith;

        return this;

    }

}
