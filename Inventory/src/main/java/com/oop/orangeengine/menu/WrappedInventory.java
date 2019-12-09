package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.BukkitItem;
import com.oop.orangeengine.menu.packet.PacketUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class WrappedInventory implements Cloneable, InventoryHolder {

    private AMenu owner;
    private Inventory bukkitInventory;
    private AMenuButton[] arrayButtons = null;
    private String title;

    public WrappedInventory(AMenu owner, String title) {
        this.owner = owner;
        this.bukkitInventory = Bukkit.createInventory(this, owner.size(), Helper.color(title));
        this.arrayButtons = new AMenuButton[bukkitInventory.getSize()];
        this.title = title;

        // Initialize items so we don't get any nulls
        loadItems();
    }

    public WrappedInventory setBukkitInventory(Inventory inventory) {
        this.bukkitInventory = inventory;
        this.arrayButtons = new AMenuButton[inventory.getSize()];
        return this;
    }

    private WrappedInventory() {}

    private void loadItems() {
        for (int slot = 0; slot < arrayButtons.length; slot++) {

            ItemStack itemStack = bukkitInventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR)
                arrayButtons[slot] = BukkitItem.newAir(slot);

            else
                arrayButtons[slot] = new BukkitItem(itemStack, slot);

        }
        ensureButtonsHaveHolder();
    }

    public Set<AMenuButton> getArrayButtons(boolean filterOutItems) {
        ensureNotEmpty();
        return Arrays.stream(arrayButtons)
                .filter(button -> filterOutItems && !(button instanceof BukkitItem))
                .collect(Collectors.toSet());
    }

    public AMenuButton getButtonAt(int slot) {
        ensureNotEmpty();
        return arrayButtons[slot];
    }

    public Set<AMenuButton> getBukkitItems() {
        ensureNotEmpty();
        return Arrays.stream(arrayButtons)
                .filter(button -> button instanceof BukkitItem)
                .collect(Collectors.toSet());
    }

    public void updateButton(AMenuButton button) {
        if (button.slot() == -1 || button.currentItem() == null) return;
        bukkitInventory.setItem(button.slot(), button.currentItem());

        Set<Player> viewers = getViewers();
        viewers.forEach(player -> PacketUtils.updateSlot(player, button.slot(), button.currentItem(), true));
    }

    public Set<Player> getViewers() {
        return Collections.unmodifiableCollection(bukkitInventory.getViewers()).stream()
                .map(he -> (Player) he)
                .collect(Collectors.toSet());
    }

    private void ensureNotEmpty() {
        if (arrayButtons == null) {
            arrayButtons = new AMenuButton[bukkitInventory.getSize()];
            loadItems();
        }
    }

    public void setButton(int slot, AMenuButton button) {
        Helper.debug("Setting " + button.currentItem() + " into s lot " + slot);
        ensureNotEmpty();
        if (button == null)
            button = BukkitItem.newAir(slot);

        button.holder(this);
        arrayButtons[slot] = button;
        button.slotNoUpdate(slot);
        updateButton(button);
    }

    public long emptySlots() {
        ensureNotEmpty();
        return Arrays.stream(arrayButtons)
                .filter(button -> (button instanceof BukkitItem && button.currentItem().getType() == Material.AIR) || button.placeholder() || !button.actAsFilled())
                .count();
    }

    public List<Integer> listEmptySlots() {
        ensureNotEmpty();
        return Arrays.stream(arrayButtons)
                .filter(button -> (button instanceof BukkitItem && button.currentItem().getType() == Material.AIR) || button.placeholder() || !button.actAsFilled())
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
    public WrappedInventory clone() {
        WrappedInventory wrappedInventory = new WrappedInventory();
        wrappedInventory.owner = owner;
        wrappedInventory.bukkitInventory = Bukkit.createInventory(wrappedInventory, owner.size(), title);
        wrappedInventory.title = title;
        Arrays.stream(arrayButtons)
                .map(AMenuButton::clone)
                .forEach(button -> wrappedInventory.setButton(button.slot(), button));
        return wrappedInventory;
    }

    public void ensureButtonsHaveHolder() {
        for (AMenuButton button : arrayButtons)
            button.holder(this);
    }

    public void removeIf(Predicate<AMenuButton> filter) {
        List<Integer> updatedSlots = new ArrayList<>();

        // Update virtually
        for (int i = 0; i < arrayButtons.length; i++) {
            AMenuButton button = arrayButtons[i];
            if (filter.test(button)) {
                arrayButtons[i] = BukkitItem.newAir(button.slot());
                updatedSlots.add(i);
            }
        }

        // Update in inventory
        for (int slot : updatedSlots)
            updateAtSlot(slot);
    }

    public void updateAtSlot(int slot) {
        AMenuButton button = arrayButtons[slot];

        if (button.slot() == -1 || button.currentItem() == null) return;
        bukkitInventory.setItem(button.slot(), button.currentItem());

        Set<Player> viewers = getViewers();
        viewers.forEach(player -> PacketUtils.updateSlot(player, button.slot(), button.currentItem(), true));
    }

    public int firstEmpty() {
        ensureNotEmpty();
        return Arrays.stream(arrayButtons)
                .filter(button -> {
                    if (button.actAsFilled())
                        return false;

                    if (button instanceof BukkitItem && button.currentItem().getType() == Material.AIR)
                        return true;

                    if (button.placeholder())
                        return true;

                    return false;
                })
                .map(AMenuButton::slot)
                .findFirst()
                .orElse(-1);
    }

    @Override
    public Inventory getInventory() {
        return getBukkitInventory();
    }

    public void moveTo(Inventory inventory) {

        // Check if we have current viewers if so add them to the list
        Set<Player> currentViewers = new HashSet<>();
        if (bukkitInventory != null && !bukkitInventory.getViewers().isEmpty())
            currentViewers.addAll(bukkitInventory.getViewers().stream().map(viewer -> (Player) viewer).collect(Collectors.toSet()));

        // Move current items to new inventory
        moveItems(inventory);

        this.bukkitInventory = inventory;
        currentViewers.forEach(this::open);
    }

    public OptionalConsumer<AMenuButton> findByFilter(Predicate<AMenuButton> buttonPredicate) {
        return OptionalConsumer.of(Arrays.asList(this.getArrayButtons()).stream().filter(buttonPredicate).findFirst());
    }

    private void moveItems(Inventory inventory) {
        if (arrayButtons != null) {
            for (AMenuButton button : arrayButtons) {
                inventory.setItem(button.slot(), button.currentItem());
            }
        }
    }

    public void changeTitle(String title) {
        this.title = title;
        if (bukkitInventory != null && !bukkitInventory.getViewers().isEmpty())
            InventoryUtil.updateTitle(bukkitInventory, title);

        else
            moveTo(Bukkit.createInventory(this, owner.size(), title));
    }

}
