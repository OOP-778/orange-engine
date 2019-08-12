package org.brian.core.mi.types;

import com.google.common.collect.HashBiMap;
import org.brian.core.mi.AMenu;
import org.brian.core.mi.MenuInventory;
import org.brian.core.mi.button.AMenuButton;
import org.brian.core.mi.button.DefaultButtons;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PagedMenu extends AMenu {

    private HashBiMap<Integer, MenuInventory> pages = HashBiMap.create();

    public PagedMenu(String title, int size) {
        super(title, size);
    }

    public PagedMenu(AMenu owner, String title, int size) {
        super(owner, title, size);
    }

    @Override
    public Inventory build() {

        List<AMenuButton> buttons = buttonList().stream().filter(AMenuButton::isPagedButton).collect(Collectors.toList());

        MenuInventory placeholderInventory = new MenuInventory(size(), this, title());
        if (menuDesigner() != null) menuDesigner().applyAsButtons(this);
        setDummies(buttonList().stream().
                filter(button -> !button.isPagedButton()).collect(Collectors.toList()), placeholderInventory);

        List<AMenuButton> withoutTemp = buttons.stream().filter(button -> !button.isTempButton()).collect(Collectors.toList());

        int currentPage = 1;

        //Checking for old pages
        pages.values().forEach(mi -> {
            mi.clear();
            mi.copy(placeholderInventory);
        });

        if (withoutTemp.isEmpty()) {

            MenuInventory menuInventory = pages.getOrDefault(currentPage, placeholderInventory);
            menuInventory(menuInventory);

            AMenuButton lastPage = menuInventory().buttons().stream().filter(Objects::nonNull).filter(b -> b.identity().equalsIgnoreCase(DefaultButtons.LAST_PAGE.identifier()) || b.actionIdentity().equalsIgnoreCase(DefaultButtons.LAST_PAGE.identifier())).findFirst().orElse(null);
            AMenuButton nextPage = menuInventory().buttons().stream().filter(Objects::nonNull).filter(b -> b.identity().equalsIgnoreCase(DefaultButtons.NEXT_PAGE.identifier()) || b.actionIdentity().equalsIgnoreCase(DefaultButtons.NEXT_PAGE.identifier())).findFirst().orElse(null);

            if (nextPage != null) nextPage.replace();
            if (lastPage != null) lastPage.replace();

            pages.put(currentPage, menuInventory());
            menuInventory().fillButtons();
            return menuInventory().bukkitInventory();

        }

        for (AMenuButton button : withoutTemp) {

            MenuInventory menuInventory;
            if (pages.containsKey(currentPage)) {
                menuInventory = pages.get(currentPage);
            } else {
                menuInventory = copyMI(placeholderInventory);
                pages.put(currentPage, menuInventory);
            }

            int emptySlot = menuInventory.firstEmptySlot(false);
            if (emptySlot != -1) menuInventory.addButton(button.slot(emptySlot));
            else {
                currentPage++;
                if (pages.containsKey(currentPage)) {
                    menuInventory = pages.get(currentPage);
                } else {
                    menuInventory = copyMI(placeholderInventory);
                    pages.put(currentPage, menuInventory);
                }
                emptySlot = menuInventory.firstEmptySlot(false);
                menuInventory.addButton(button.slot(emptySlot));
            }
        }

        Set<Integer> toRemove = new HashSet<>();
        pages.forEach((k, v) -> {
            if (v.isEmpty()) toRemove.add(k);
        });

        toRemove.forEach(page -> pages.remove(page));

        pages.forEach((page, mi) -> {

            AMenuButton lastPage = mi.buttons().stream().filter(Objects::nonNull).filter(b -> b.identity().equalsIgnoreCase(DefaultButtons.LAST_PAGE.identifier()) || b.actionIdentity().equalsIgnoreCase(DefaultButtons.LAST_PAGE.identifier())).findFirst().orElse(null);
            AMenuButton nextPage = mi.buttons().stream().filter(Objects::nonNull).filter(b -> b.identity().equalsIgnoreCase(DefaultButtons.NEXT_PAGE.identifier()) || b.actionIdentity().equalsIgnoreCase(DefaultButtons.NEXT_PAGE.identifier())).findFirst().orElse(null);

            if (lastPage != null) {
                if (page == 1)
                    lastPage.replace();
            }

            if (page == pages.size() && nextPage != null)
                nextPage.replace();

        });

        pages.inverse().keySet().forEach(mi -> {

            mi.fillButtons();
            mi.updateInventory();

        });

        menuInventory(pages.get(1));
        return menuInventory().bukkitInventory();

    }

    public MenuInventory bukkitToMI(Inventory inventory) {
        return pages.inverse().keySet().stream().filter(mi -> mi.bukkitInventory().equals(inventory)).findFirst().orElse(null);
    }

    public AMenuButton findButtonByItemStack(Inventory inventory, ItemStack itemStack) {

        MenuInventory mi = bukkitToMI(inventory);
        AMenuButton button = null;

        if (mi != null)
            button = mi.buttons().stream().filter(b -> b.itemStack().isSimilar(itemStack)).findFirst().orElse(null);

        return button;

    }

    public AMenuButton findButtonBySlot(Inventory inventory, int slot) {

        MenuInventory mi = bukkitToMI(inventory);
        AMenuButton button = null;

        if (mi != null) button = mi.buttons().stream().filter(b -> b.slot() == slot).findFirst().orElse(null);

        return button;

    }

    public HashBiMap<Integer, MenuInventory> pages() {
        return pages;
    }

    public MenuInventory nextPage(MenuInventory menuInventory) {
        return pages.get(pages.inverse().get(menuInventory) + 1);
    }

    public MenuInventory lastPage(MenuInventory menuInventory) {
        return pages.get(pages.inverse().get(menuInventory) - 1);
    }

    public MenuInventory copyMI(MenuInventory mi) {

        MenuInventory newMenu = new MenuInventory(mi.size(), mi.owner(), mi.title());
        newMenu.buttons(mi.buttons().stream().filter(b -> !b.isPagedButton()).map(AMenuButton::clone).collect(Collectors.toList()));

        newMenu.fillButtons();

        return newMenu;

    }

    public int page(MenuInventory menuInventory) {
        return pages().inverse().get(menuInventory);
    }
}
