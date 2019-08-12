package org.brian.core.mi;

import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.button.MenuItem;
import org.brian.core.mi.packet.UpdateSlot;
import org.brian.core.utils.Storagable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.brian.core.utils.Helper.color;
import static org.brian.core.utils.Helper.print;

public class MenuInventory extends Storagable implements Cloneable {

    private Set<AMenuButton> menuButtons;
    private Inventory inventory;
    private int size;
    private AMenu owner;
    private String title;

    public MenuInventory(int size, AMenu owner, String title) {

        this.menuButtons = ConcurrentHashMap.newKeySet();
        this.size = size;
        this.title = title;
        this.owner = owner;

        this.inventory = Bukkit.createInventory(owner, size, color(title));

    }

    public MenuInventory(int size, AMenu owner) {
        this(size, owner, "Undefined Title");
    }

    public void fillButtons() {

        if (inventory == null) return;

        menuButtons.forEach(button -> {
            if (button.slot() == -1) return;
            if (!button.holders().contains(this)) button.holders().add(this);
            inventory.setItem(button.slot(), button.itemStack());
        });
    }

    public void changeTitle(String newTitle) {

        List<Player> oldViewers = new ArrayList<>();

        if (inventory != null) {

            //Because inventory isn't null, then someone might have opened it, let's check if there's someone viewing it rn.
            if (!inventory.getViewers().isEmpty()) {
                oldViewers.addAll(inventory.getViewers().stream().map(h -> (Player) h).collect(toList()));
            }

        }

        this.title = newTitle;
        Inventory newInventory = Bukkit.createInventory(owner, size, color(title));

        oldViewers.forEach(player -> {
            if (player.getOpenInventory().getTopInventory() != null || player.getOpenInventory().getTopInventory() == inventory)
                player.openInventory(newInventory);

        });

        this.inventory = newInventory;

    }

    public int size() {
        return size;
    }

    public String title() {
        return title;
    }

    public Inventory bukkitInventory() {
        return inventory;
    }

    public AMenu owner() {
        return owner;
    }

    public MenuInventory item(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    public MenuInventory addButton(AMenuButton button) {

        menuButtons.removeIf(b -> b.slot() == button.slot());
        menuButtons.add(button);
        button.addHolder(this);
        return this;

    }

    public MenuInventory addButton(AMenuButton button, boolean set) {

        menuButtons.removeIf(b -> b.slot() == button.slot());
        menuButtons.add(button);
        button.addHolder(this);

        if (set && inventory != null) inventory.setItem(button.slot(), button.itemStack());

        return this;

    }

    public List<AMenuButton> buttons() {
        return new ArrayList<>(menuButtons);
    }

    public MenuInventory buttons(List<AMenuButton> menuButtons) {
        this.menuButtons.clear();
        this.menuButtons.addAll(menuButtons);
        menuButtons.forEach(b -> b.addHolder(this));
        return this;
    }

    public List<AMenuButton> buttons(boolean includeEmpty, Predicate<AMenuButton> filter) {

        List<AMenuButton> buttonList = new ArrayList<>();

        IntStream.range(0, size()).
                forEach(slot -> {

                    AMenuButton button = menuButtons.stream().filter(b -> b.slot() == slot).findFirst().orElse(null);
                    if (button == null) {

                        ItemStack itemStack = inventory.getItem(slot);
                        if (itemStack == null || itemStack.getType() == Material.AIR) {

                            if (includeEmpty) buttonList.add(new MenuItem(new ItemStack(Material.AIR), slot));
                            return;

                        }

                        AMenuButton itemButton = new MenuItem(itemStack, slot);
                        if (filter != null && !filter.test(itemButton)) return;
                        buttonList.add(itemButton);

                    } else if (filter != null && filter.test(button)) buttonList.add(button);

                });

        return buttonList;

    }

    public List<MenuItem> items(boolean includeEmpty, Predicate<MenuItem> filter) {

        List<MenuItem> items = new ArrayList<>();

        IntStream.range(0, size()).
                forEach(slot -> {

                    AMenuButton button = menuButtons.stream().filter(b -> b.slot() == slot).findFirst().orElse(null);
                    ItemStack itemStack = inventory.getItem(slot);

                    if (button == null) {

                        if (itemStack == null || itemStack.getType() == Material.AIR) {

                            if (includeEmpty) items.add(new MenuItem(new ItemStack(Material.AIR), slot));
                            return;

                        }

                        MenuItem item = new MenuItem(itemStack, slot);
                        if (filter != null && !filter.test(item)) return;
                        items.add(item);

                    } else {

                        MenuItem menuItem = new MenuItem(itemStack, slot);
                        if ((itemStack == null || itemStack.getType() == Material.AIR)) {
                            if (includeEmpty) items.add(new MenuItem(new ItemStack(Material.AIR), slot));
                        } else {
                            if (!button.isCancelEvent()) {
                                if (filter != null && !filter.test(menuItem)) return;
                                items.add(menuItem);
                            }
                        }
                    }

                });
        return items;
    }

    public List<MenuItem> items(boolean includeEmpty) {
        return items(includeEmpty, null);
    }

    public List<AMenuButton> buttons(boolean includeEmpty) {
        return buttons(includeEmpty, null);
    }

    public int firstEmptySlot(boolean items) {

        //Temp buttons
        if (!items) {

            AMenuButton button = buttons().stream().filter(AMenuButton::isTempButton).min(Comparator.comparing(AMenuButton::slot)).orElse(null);
            if (button != null)
                return button.slot();

            AMenuButton menuItem = buttons(true).stream().filter(b -> b.itemStack().getType() == Material.AIR).min(Comparator.comparing(AMenuButton::slot)).orElse(null);
            return menuItem == null ? -1 : menuItem.slot();

        } else {

            AMenuButton button = buttons().stream().filter(AMenuButton::isTempButton).min(Comparator.comparing(AMenuButton::slot)).orElse(null);
            if (button != null)
                return button.slot();

            AMenuButton menuItem = items(true).stream().filter(b -> b.itemStack().getType() == Material.AIR).min(Comparator.comparing(AMenuButton::slot)).orElse(null);
            return menuItem == null ? -1 : menuItem.slot();

        }

    }

    public int emptySlots() {

        int freeSlotsCount = 0;
        freeSlotsCount += buttons().stream().filter(AMenuButton::isTempButton).count();
        freeSlotsCount += items(true).stream().filter(b -> b.itemStack().getType() == Material.AIR).count();

        return freeSlotsCount;
    }

    public MenuInventory updateInventory() {

        if (bukkitInventory() != null) {
            Set<Player> viewers = new ConcurrentHashMap<Player, String>().newKeySet();
            viewers.addAll(bukkitInventory().getViewers().stream().map(p -> (Player) p).collect(Collectors.toList()));
            viewers.forEach(Player::updateInventory);
        }

        return this;

    }

    public MenuInventory updateSlot(int slot, ItemStack item) {

        if (bukkitInventory() != null) {
            Set<Player> viewers = new ConcurrentHashMap<Player, String>().newKeySet();
            viewers.addAll(bukkitInventory().getViewers().stream().map(p -> (Player) p).collect(Collectors.toList()));
            viewers.forEach(p -> UpdateSlot.update(p, slot, item));
        }

        return this;

    }

    public MenuInventory updateButton(AMenuButton aMenuButton) {

        if (menuButtons.contains(aMenuButton)) {
            if (aMenuButton.itemStack() == null || aMenuButton.itemStack().getType() == Material.AIR) {

                buttons().removeIf(button -> button.slot() == aMenuButton.slot());
                if (bukkitInventory() != null) {
                    bukkitInventory().setItem(aMenuButton.slot(), new ItemStack(Material.AIR));
                    updateSlot(aMenuButton.slot(), new ItemStack(Material.AIR));
                }

            } else {

                if (bukkitInventory() != null) {
                    bukkitInventory().setItem(aMenuButton.slot(), aMenuButton.itemStack());
                    updateSlot(aMenuButton.slot(), aMenuButton.itemStack());
                }

            }
        }

        return this;

    }

    public void clear() {

        if (inventory != null) inventory.clear();
        menuButtons.clear();

    }

    public void copy(MenuInventory placeholderInventory) {

        buttons(placeholderInventory.buttons().stream().filter(b -> !b.isPagedButton()).map(AMenuButton::clone).collect(Collectors.toList()));

    }

    public void printButtons() {

        print("Slot       Identity       ItemStack");
        buttons().forEach(b -> print(b.slot() + "       " + b.identity() + "       " + b.itemStack()));

    }

    public boolean isEmpty() {
        return buttons().stream().
                filter(b -> !b.isTempButton()).
                filter(b -> !b.isFiller()).count() == 0;
    }

    public void replaceButton(AMenuButton button, AMenuButton replaceButton) {

        replaceButton.slot(button.slot());
        addButton(replaceButton, false);

        if (bukkitInventory() != null) {
            bukkitInventory().setItem(replaceButton.slot(), replaceButton.itemStack());
            updateSlot(button.slot(), replaceButton.itemStack());
        }
        owner.replaceButton(button, replaceButton);

    }
}
