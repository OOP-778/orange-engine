package org.brian.core.mi.button;

import org.brian.core.mi.AMenu;
import org.brian.core.mi.MenuInventory;
import org.brian.core.mi.events.ButtonClickEvent;
import org.brian.core.mi.types.PagedMenu;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public enum DefaultButtons {

    RETURN("return", event -> {

        AMenu inventoryHolder = event.menuInventory().owner();
        if (inventoryHolder.parent() != null) {

            Inventory inventory = inventoryHolder.parent().getInventory();
            if (inventory != null)
                event.player().openInventory(inventory);
        }

    }),
    LAST_PAGE("last page", event -> {

        if (event.menuInventory().owner() instanceof PagedMenu) {

            PagedMenu pagedMenu = (PagedMenu) event.menuInventory().owner();
            MenuInventory lastPage = pagedMenu.lastPage(event.menuInventory());

            if (lastPage != null && lastPage.bukkitInventory() != null)
                event.player().openInventory(lastPage.bukkitInventory());

        }

    }),
    NEXT_PAGE("next page", event -> {

        if (event.menuInventory().owner() instanceof PagedMenu) {

            PagedMenu pagedMenu = (PagedMenu) event.menuInventory().owner();
            MenuInventory nextPage = pagedMenu.nextPage(event.menuInventory());

            if (nextPage != null && nextPage.bukkitInventory() != null)
                event.player().openInventory(nextPage.bukkitInventory());

        }

    }),
    FILLER("filler", null, (button) -> {
        button.isFiller(true);
    }),
    OPEN("open", event -> {

        AMenu inventoryHolder = event.menuInventory().owner();
        String menuName = event.button().getData(event.button().identity());

        if (menuName != null) {

            AMenu menu = inventoryHolder.allMenus().get(menuName);
            if (menu == null) return;

            event.player().openInventory(menu.getInventory());

        }

    });

    private String identifier;
    private Consumer<ButtonClickEvent> clickEvent;
    private Consumer<AMenuButton> buttonProvider;

    DefaultButtons(String identifier, Consumer<ButtonClickEvent> clickEvent) {
        this(identifier, clickEvent, null);
    }

    DefaultButtons(String identifier, Consumer<ButtonClickEvent> clickEvent, Consumer<AMenuButton> buttonProvider) {
        this.clickEvent = clickEvent;
        this.identifier = identifier;
        this.buttonProvider = buttonProvider;
    }

    public AMenuButton getButtonOfItemStack(ItemStack item) {

        if (item == null) return null;
        MenuButton button = new MenuButton(item, -1);
        button.identity(identifier());
        if (clickEvent() != null) button.clickEvent(clickEvent());
        if (buttonProvider() != null) buttonProvider().accept(button);

        return button;

    }

    public AMenuButton getButtonOfItemStack(ItemStack item, Object value) {

        if (item == null) return null;
        MenuButton button = new MenuButton(item, -1);
        button.identity(identifier());
        if (clickEvent() != null) button.clickEvent(clickEvent());
        if (buttonProvider() != null) buttonProvider().accept(button);

        button.putData(identifier(), value);

        return button;

    }

    public Consumer<AMenuButton> buttonProvider() {
        return buttonProvider;
    }

    public Consumer<ButtonClickEvent> clickEvent() {
        return clickEvent;
    }

    public String identifier() {
        return identifier;
    }

}
