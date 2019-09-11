package com.oop.orangeengine.menu.events;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ButtonEmptyEvent extends ButtonClickEvent {

    private final ItemStack emptied;

    public ButtonEmptyEvent(ButtonClickEvent buttonClickEvent, ItemStack emptied) {
        super(buttonClickEvent.getWrappedInventory(), buttonClickEvent.getMenu(), buttonClickEvent.getOriginalEvent(), buttonClickEvent.getPlayer(), buttonClickEvent.getClickedButton(), buttonClickEvent.getBeforeItem());
        this.emptied = emptied;
    }

}
