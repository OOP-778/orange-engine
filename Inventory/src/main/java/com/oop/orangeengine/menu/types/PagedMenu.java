package com.oop.orangeengine.menu.types;

import com.google.common.collect.HashBiMap;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

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

    }

    @Override
    public WrappedInventory getWrapperFromBukkit(Inventory inventory) {
        return pages.values().stream()
                .filter(wi -> wi.getBukkitInventory() == inventory)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected Inventory provideNewInv() {
        return Bukkit.createInventory(this, size(), title());
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
