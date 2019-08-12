package org.brian.core.mi.events;

import org.brian.core.mi.MenuInventory;
import org.brian.core.mi.button.AMenuButton;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ButtonClickEvent extends Event {

    private static HandlerList handlerList = new HandlerList();
    private MenuInventory menuInventory;
    private AMenuButton button;
    private Player player;
    private InventoryClickEvent realEvent;
    private boolean isCancelled = false;

    public ButtonClickEvent(MenuInventory menuInventory, AMenuButton button, Player player, InventoryClickEvent realEvent) {

        this.menuInventory = menuInventory;
        this.player = player;
        this.realEvent = realEvent;
        this.button = button;

    }

    public Player player() {
        return player;
    }

    public InventoryClickEvent realEvent() {
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

    public AMenuButton button() {
        return button;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
