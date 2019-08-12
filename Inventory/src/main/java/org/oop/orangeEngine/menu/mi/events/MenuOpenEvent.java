package org.brian.core.mi.events;

import org.brian.core.mi.MenuInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class MenuOpenEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private MenuInventory menuInventory;
    private Inventory bukkitInventory;
    private Player player;
    private InventoryOpenEvent realEvent;
    private boolean isCancelled = false;

    public MenuOpenEvent(MenuInventory menuInventory, InventoryOpenEvent realEvent) {

        this.menuInventory = menuInventory;
        this.player = (Player) realEvent.getPlayer();
        this.realEvent = realEvent;
        this.bukkitInventory = realEvent.getInventory();

    }

    public Player player() {
        return player;
    }

    public InventoryOpenEvent realEvent() {
        return realEvent;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancelEvent() {
        isCancelled = true;
    }

    public MenuInventory menuInventory() {
        return menuInventory;
    }

    public Inventory bukkitInventory() {
        return bukkitInventory;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
