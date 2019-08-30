package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

@RequiredArgsConstructor
@Getter
public class ButtonClickEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private final WrappedInventory wrappedInventory;
    private final AMenu menu;
    private final InventoryClickEvent originalEvent;
    private final Player player;
    private boolean cancelled = false;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
