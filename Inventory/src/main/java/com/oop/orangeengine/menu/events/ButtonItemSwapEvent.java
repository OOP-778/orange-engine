package com.oop.orangeengine.menu.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class ButtonItemSwapEvent extends ButtonEvent {

    private ItemStack swappedWith;
    private ItemStack previous;

    private static HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
