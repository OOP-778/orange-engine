package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import static com.oop.orangeengine.main.Helper.color;

public class FillableButton extends AMenuButton {

    @Getter
    @Setter
    private FillEvent fillEvent;

    public FillableButton(ItemStack currentItem, int slot) {
        super(currentItem, slot);

        clickHandler(event -> event.getPlayer().sendMessage(color("You filled me with " + event.getOriginalEvent().getClickedInventory().getItem(slot()))));
    }

    public static interface FillEvent {
        void handle(ItemStack filled, WrappedInventory inventory, AMenu menu);
    }

}
