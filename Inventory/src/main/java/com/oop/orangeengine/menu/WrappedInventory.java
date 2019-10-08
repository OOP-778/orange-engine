package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;
import com.oop.orangeengine.menu.packet.SlotUpdate;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class WrappedInventory implements Cloneable {

    private AMenu owner;
    private Inventory bukkitInventory;
    private AMenuButton[] buttons;

    public WrappedInventory(AMenu owner, Inventory inventory) {
        this.owner = owner;
        this.bukkitInventory = inventory;
        this.buttons = new AMenuButton[inventory.getSize()];

        // Initialize items so we don't get any nulls
        loadItems();
    }

    private WrappedInventory() {}

    private void loadItems() {
        for (int slot = 0; slot < buttons.length; slot++) {

            ItemStack itemStack = bukkitInventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR)
                buttons[slot] = BukkitItem.newAir(slot);

            else
                buttons[slot] = new BukkitItem(itemStack, slot);

        }
        ensureButtonsHaveHolder();
    }

    public Set<AMenuButton> getButtons(boolean filterOutItems) {
        ensureNotEmpty();
        return Arrays.stream(buttons)
                .filter(button -> filterOutItems && !(button instanceof BukkitItem))
                .collect(Collectors.toSet());
    }

    public AMenuButton getButtonAt(int slot) {
        ensureNotEmpty();
        return buttons[slot];
    }

    public Set<AMenuButton> getBukkitItems() {
        ensureNotEmpty();
        return Arrays.stream(buttons)
                .filter(button -> button instanceof BukkitItem)
                .collect(Collectors.toSet());
    }

    public void updateButton(AMenuButton button) {
        if (button.slot() == -1 || button.currentItem() == null) return;
        bukkitInventory.setItem(button.slot(), button.currentItem());

        Set<Player> viewers = getViewers();
        viewers.forEach(player -> SlotUpdate.update(player, button.slot(), button.currentItem(), true));
    }

    public Set<Player> getViewers() {
        return Collections.unmodifiableCollection(bukkitInventory.getViewers()).stream()
                .map(he -> (Player) he)
                .collect(Collectors.toSet());
    }

    private void ensureNotEmpty() {
        if(buttons == null) {
            buttons = new AMenuButton[bukkitInventory.getSize()];
            loadItems();
        }
    }

    public void setButton(int slot, AMenuButton button) {
        ensureNotEmpty();
        if (button == null)
            button = BukkitItem.newAir(slot);

        button.holder(this);
        buttons[slot] = button;
        updateButton(button);
    }

    public long emptySlots() {
        ensureNotEmpty();
        return Arrays.stream(buttons)
                .filter(button -> button.currentItem().getType() == Material.AIR)
                .count();
    }

    public List<Integer> listEmptySlots() {
        ensureNotEmpty();
        return Arrays.stream(buttons)
                .filter(button -> button.currentItem().getType() == Material.AIR || button.placeholder())
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

    @Override
    protected WrappedInventory clone() {
        WrappedInventory wrappedInventory = new WrappedInventory();
        wrappedInventory.owner = owner;
        wrappedInventory.bukkitInventory = owner.provideNewInv();
        Arrays.stream(buttons)
                .map(AMenuButton::clone)
                .forEach(button -> wrappedInventory.setButton(button.slot(), button));
        return wrappedInventory;
    }

    public void ensureButtonsHaveHolder() {
        for (AMenuButton button : buttons)
            button.holder(this);
    }

}
