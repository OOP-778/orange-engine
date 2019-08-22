package com.oop.orangeengine.menu.types;

import com.oop.orangeengine.menu.AMenu;

public class BasicMenu extends AMenu {
    public BasicMenu(String identifier, int size, AMenu parent) {
        super(identifier, size, parent);
    }

    public BasicMenu(String identifier, int size) {
        super(identifier, size);
    }

    @Override
    protected void build() {

    }
}
