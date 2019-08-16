package com.oop.orangeengine.menu;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

@Getter
public class WrappedInventory {

    private AMenu owner;
    private Inventory bukkitInventory;
    private AMenuButton[] buttons;

    public WrappedInventory(AMenu owner, Inventory inventory) {

        this.owner = owner;
        this.bukkitInventory = inventory;
        this.buttons = new AMenuButton[inventory.getSize()];
        updateItems();

    }

    public void updateItems() {

        for (int slot = 0; slot < buttons.length; slot++) {

            ItemStack itemStack = bukkitInventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR)
                buttons[slot] = BukkitItem.newAir(slot);

            //else
                //buttons[slot] = new BukkitItem(itemStack, slot);

        }

    }

    public void updateSlot(int slot) {
    }

    public Set<AMenuButton> getButtons() {
        return new HashSet<>();
    }

    public Set<AMenuButton> getItems() {
        return new HashSet<>();
    }


    public void updateButton(AMenuButton button) {



    }
}
