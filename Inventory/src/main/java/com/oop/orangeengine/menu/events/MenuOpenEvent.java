package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.AMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

@Getter
@RequiredArgsConstructor
public class MenuOpenEvent {

    private final AMenu menu;
    private final InventoryOpenEvent originalEvent;

}
