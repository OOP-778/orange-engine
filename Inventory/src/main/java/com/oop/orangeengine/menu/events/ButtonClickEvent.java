package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;

@AllArgsConstructor
@Getter
public class ButtonClickEvent {

    private WrappedInventory wrappedInventory;
    private AMenu menu;
    private InventoryClickEvent originalEvent;

}
