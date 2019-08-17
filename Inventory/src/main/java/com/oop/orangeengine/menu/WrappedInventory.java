package com.oop.orangeengine.menu;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;
import com.oop.orangeengine.menu.packet.SlotUpdate;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class WrappedInventory {

    private AMenu owner;
    private Inventory bukkitInventory;
    private AMenuButton[] buttons;

    public WrappedInventory(AMenu owner, Inventory inventory) {

        this.owner = owner;
        this.bukkitInventory = inventory;

        this.buttons = new AMenuButton[inventory.getSize()];
        loadItems();

    }

    private void loadItems() {
        for (int slot = 0; slot < buttons.length; slot++) {

            ItemStack itemStack = bukkitInventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR)
                buttons[slot] = BukkitItem.newAir(slot);

            else
                buttons[slot] = new BukkitItem(itemStack, slot);

        }
    }

    public Set<AMenuButton> getButtons(boolean filterOutItems) {
        return Arrays.asList(buttons).stream()
                .filter(button -> filterOutItems && !(button instanceof BukkitItem))
                .collect(Collectors.toSet());
    }

    public Set<AMenuButton> getItems() {
        return Arrays.asList(buttons).stream()
                .filter(button -> button instanceof BukkitItem)
                .collect(Collectors.toSet());
    }

    public void updateButton(AMenuButton button) {
        if (button.slot() == -1 || button.currentItem() == null) return;
        bukkitInventory.setItem(button.slot(), button.currentItem());

        Set<Player> viewers = getViewers();
        viewers.forEach(player -> SlotUpdate.update(player, button.slot(), button.currentItem()));
    }

    public Set<Player> getViewers() {
        return Collections.unmodifiableCollection(bukkitInventory.getViewers()).stream()
                .map(he -> (Player) he)
                .collect(Collectors.toSet());
    }

    public void setButton(int slot, AMenuButton button) {
        buttons[slot] = button;
        updateButton(button);
    }

    public long emptySlots() {
        return Arrays.asList(buttons).stream()
                .filter(button -> button.currentItem().getType() == Material.AIR)
                .count();
    }

    public void open(Player player) {
        player.openInventory(bukkitInventory);
    }

}
