package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.AMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.inventory.InventoryCloseEvent;

@Getter
@RequiredArgsConstructor
public class MenuCloseEvent {

    private final AMenu menu;
    private final InventoryCloseEvent originalEvent;

}
