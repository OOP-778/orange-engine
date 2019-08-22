package com.oop.orangeengine.menu.types;

import com.google.common.collect.HashBiMap;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;

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

    public OptionalConsumer<WrappedInventory> getNextPage(WrappedInventory wrappedInventory) {
        return getPage(pages.inverse().get(wrappedInventory) + 1);
    }

    public OptionalConsumer<WrappedInventory> getPreviousPage(WrappedInventory wrappedInventory) {
        return getPage(pages.inverse().get(wrappedInventory) - 1);
    }

    private OptionalConsumer<WrappedInventory> getPage(int page) {
        return OptionalConsumer.of(pages.get(page));
    }

}
