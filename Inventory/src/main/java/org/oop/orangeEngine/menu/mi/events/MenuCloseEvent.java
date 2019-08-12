package org.brian.core.mi.events;

import org.brian.core.mi.MenuInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class MenuCloseEvent extends Event {

    private static HandlerList handlerList = new HandlerList();
    private MenuInventory menuInventory;
    private Inventory bukkitInventory;
    private Player player;
    private InventoryCloseEvent realEvent;
    private boolean isCancelled = false;
    private boolean isFinal;

    public MenuCloseEvent(MenuInventory menuInventory, InventoryCloseEvent realEvent, boolean isFinal) {

        this.menuInventory = menuInventory;
        this.player = (Player) realEvent.getPlayer();
        this.realEvent = realEvent;
        this.isFinal = isFinal;
        this.bukkitInventory = realEvent.getInventory();

    }

    public Player player() {
        return player;
    }

    public InventoryCloseEvent realEvent() {
        return realEvent;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancelEvent() {
        isCancelled = true;
    }

    public boolean isFinal() {
        return isFinal;
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
