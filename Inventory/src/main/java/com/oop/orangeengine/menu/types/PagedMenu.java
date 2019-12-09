package com.oop.orangeengine.menu.types;

import com.google.common.collect.HashBiMap;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.SwappableButton;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import javax.swing.plaf.basic.BasicGraphicsUtils;

public class PagedMenu extends AMenu {

    private HashBiMap<Integer, WrappedInventory> pages = HashBiMap.create();

    public PagedMenu(String identifier, int size, AMenu parent) {
        super(identifier, size, parent);
    }

    public PagedMenu(String identifier, int size) {
        super(identifier, size);
    }

    @Override
    protected void build() {
        if (designer() != null)
            designer().fill(this);

        WrappedInventory placeholderInventory = new WrappedInventory(this, title());
        for (AMenuButton button : buttons()) {
            if (button.paged()) continue;
            if (button.template()) continue;

            placeholderInventory.setButton(button.slot(), button);
        }

        OPair<Integer, WrappedInventory> currentPage = null;
        for (AMenuButton button : buttons()) {
            if (!button.paged()) continue;

            Helper.debug("Adding " + button.currentItem() + " into paged menu!");

            // Check if it's first time
            if (pages.isEmpty()) {
                currentPage = new OPair<>(1, placeholderInventory.clone());
                pages.put(1, currentPage.getValue());
            }

            // Check if currentPage is null
            if (currentPage == null) {
                WrappedInventory page = pages.get(1);
                if (page == null) {
                    currentPage = new OPair<>(1, placeholderInventory.clone());
                    pages.put(1, currentPage.getValue());

                } else {
                    currentPage = new OPair<>(1, page);
                }
            }

            Helper.debug("Current page first empty = " + currentPage.getValue().firstEmpty());
            // Check if the currentPage has any empty slots
            if (currentPage.getValue().firstEmpty() == -1) {
                WrappedInventory nextPage = pages.get(currentPage.getKey() + 1);
                if (nextPage == null) {
                    currentPage = new OPair<>(currentPage.getFirst() + 1, placeholderInventory.clone());
                    pages.put(currentPage.getKey(), currentPage.getValue());

                } else {
                    currentPage = new OPair<>(currentPage.getFirst() + 1, nextPage);
                }
            }

            Helper.debug("Added " + button.currentItem() + " into paged menu!");
            currentPage.getValue().setButton(currentPage.getValue().firstEmpty(), button);
        }

        pages.forEach((page, inv) -> {

            // Change title of the inventory according to the page number
            inv.changeTitle(inv.getTitle().replace("%currentPage%", page + "").replace("%allPages%", pages.size() + ""));

            SwappableButton previousPageButton =   inv.findByFilter(button -> button.appliedActions().contains("previous page")).get(SwappableButton.class);
            SwappableButton nextPageButton = inv.findByFilter(button -> button.appliedActions().contains("next page")).get(SwappableButton.class);

            if (previousPageButton != null && previousPageButton.isSwapped())
                previousPageButton.swap();

            if (nextPageButton != null && nextPageButton.isSwapped())
                nextPageButton.swap();

            // Swap next / last page buttons
            if(page == 1 && previousPageButton != null)
                previousPageButton.swap();

            if (page == pages.size() && nextPageButton != null)
                nextPageButton.swap();
        });

        wrappedInventory = pages.get(1);
    }

    @Override
    public WrappedInventory getWrapperFromBukkit(Inventory inventory) {
        return pages.values().stream()
                .filter(wi -> wi == inventory.getHolder())
                .findFirst()
                .orElse(null);
    }

    public OptionalConsumer<WrappedInventory> getNextPage(WrappedInventory wrappedInventory) {
        return getPage(pages.inverse().get(wrappedInventory) + 1);
    }

    public int getCurrentPage(WrappedInventory wrappedInventory) {
        return pages.inverse().get(wrappedInventory);
    }

    public OptionalConsumer<WrappedInventory> getPreviousPage(WrappedInventory wrappedInventory) {
        return getPage(pages.inverse().get(wrappedInventory) - 1);
    }

    private OptionalConsumer<WrappedInventory> getPage(int page) {
        return OptionalConsumer.of(pages.get(page));
    }

}
