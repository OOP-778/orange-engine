package org.brian.core.mi.types;

import org.brian.core.mi.AMenu;
import org.brian.core.mi.MenuInventory;
import org.brian.core.mi.button.AMenuButton;
import org.bukkit.inventory.Inventory;

public class StandardMenu extends AMenu {

    public StandardMenu(AMenu owner, String title, int size) {
        super(owner, title, size);
    }

    public StandardMenu(String title, int size) {
        super(title, size);
    }

    @Override
    public Inventory build() {

        if (menuDesigner() != null && buttonList().stream().noneMatch(AMenuButton::isFiller))
            menuDesigner().applyAsButtons(this);
        if (!isMenuInvPresent()) {
            menuInventory(new MenuInventory(size(), this, title()));
        }
        setDummies(buttonList(), menuInventory());
        menuInventory().fillButtons();
        return menuInventory().bukkitInventory();

    }
}
