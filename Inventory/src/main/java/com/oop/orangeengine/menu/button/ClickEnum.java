package com.oop.orangeengine.menu.button;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public enum ClickEnum {

    RIGHT,
    LEFT,
    GLOBAL,
    SHIFT_RIGHT,
    SHIFT_LEFT;

    public static ClickEnum match(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if (clickType.isShiftClick()) {
            if (clickType.isRightClick())
                return SHIFT_RIGHT;

            else if (clickType.isLeftClick())
                return SHIFT_LEFT;

        } else
            if(clickType.isRightClick())
                return RIGHT;

            else
                return LEFT;

        return GLOBAL;

    }
}
