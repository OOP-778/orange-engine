package com.oop.orangeengine.menu.types;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import static com.oop.orangeengine.main.Engine.getEngine;

public class BasicMenu extends AMenu {
    public BasicMenu(String identifier, int size, AMenu parent) {
        super(identifier, size, parent);
    }

    public BasicMenu(String identifier, int size) {
        super(identifier, size);
    }

    @Override
    protected void build() {
        // Apply designer
        if (designer() != null)
            designer().fill(this);

        // Set Wrapped Inventory
        if (wrappedInventory == null) {
            Inventory inventory = provideNewInv();
            wrappedInventory = new WrappedInventory(this, inventory);
        }

        // Set buttons
        buttons().forEach(button -> wrappedInventory.setButton(button.slot(), button));
    }

    @Override
    public WrappedInventory getWrapperFromBukkit(Inventory inventory) {
        return getWrappedInventory();
    }

    @Override
    protected Inventory provideNewInv() {
        return Bukkit.createInventory(this, size(), Helper.color(title()));
    }
}
