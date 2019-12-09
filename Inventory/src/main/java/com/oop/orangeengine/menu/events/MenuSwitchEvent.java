package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.AMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.logging.Handler;

@Getter
@RequiredArgsConstructor
public class MenuSwitchEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private final AMenu currentMenu;
    private final AMenu nextMenu;
    private final Player player;

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
