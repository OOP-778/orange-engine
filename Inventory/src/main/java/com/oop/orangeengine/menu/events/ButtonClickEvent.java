package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class ButtonClickEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private final WrappedInventory wrappedInventory;
    private final AMenu menu;
    private final InventoryClickEvent originalEvent;
    private final Player player;
    private boolean cancelled = false;
    private final AMenuButton clickedButton;
    private final ItemStack beforeItem;
    private final ClickEnum clickType;

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

    public void pickupAtSlot() {
        int slot = originalEvent.getSlot();
        ItemStack cursor = getOriginalEvent().getWhoClicked().getItemOnCursor().clone();

        ItemStack atSlot = originalEvent.getClickedInventory().getItem(slot);
        if (atSlot == null) return;

        atSlot = atSlot.clone();
        if (atSlot.isSimilar(cursor))
            atSlot.setAmount(atSlot.getAmount() + cursor.getAmount());

        getOriginalEvent().getClickedInventory().setItem(slot, OMaterial.AIR.parseItem());
        getOriginalEvent().getWhoClicked().setItemOnCursor(atSlot.clone());

        clickedButton.updateButtonFromHolder();
    }

    public void switchCursorWithSlot() {
        int slot = originalEvent.getSlot();
        ItemStack cursor = getOriginalEvent().getWhoClicked().getItemOnCursor().clone();
        if (cursor.getType() != Material.AIR) return;

        ItemStack atSlot = originalEvent.getClickedInventory().getItem(slot);
        if (atSlot == null) return;

        getOriginalEvent().getClickedInventory().setItem(slot, cursor.clone());
        getOriginalEvent().getWhoClicked().setItemOnCursor(atSlot.clone());
        clickedButton.updateButtonFromHolder();
    }

    public void switchCursorWith(ItemStack itemStack) {
        ItemStack cursor = getOriginalEvent().getWhoClicked().getItemOnCursor().clone();
        if (cursor.getType() != Material.AIR) return;

        getOriginalEvent().getWhoClicked().setItemOnCursor(itemStack.clone());
    }
}
