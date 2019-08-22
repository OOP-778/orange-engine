package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Helper;
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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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

        //Initialize items so we don't get any nulls
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
        return Arrays.stream(buttons)
                .filter(button -> filterOutItems && !(button instanceof BukkitItem))
                .collect(Collectors.toSet());
    }

    public Set<AMenuButton> getBukkitItems() {
        return Arrays.stream(buttons)
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
        return Arrays.stream(buttons)
                .filter(button -> button.currentItem().getType() == Material.AIR)
                .count();
    }

    public List<Integer> listEmptySlots() {
        //TODO add if button is placeholder it acts as empty slot
        return Arrays.stream(buttons)
                .filter(button -> button.currentItem().getType() == Material.AIR)
                .map(AMenuButton::slot)
                .collect(Collectors.toList());
    }

    public void open(Player player) {
        player.openInventory(bukkitInventory);
    }

    public void openToAll(Predicate<Player> filter) {
        Helper.getOnlinePlayers().stream()
                .filter(player -> filter != null && filter.test(player))
                .forEach(this::open);
    }

    public void openToAll() {
        openToAll(null);
    }

}
