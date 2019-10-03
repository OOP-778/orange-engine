package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import com.oop.orangeengine.main.util.data.set.OSet;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.types.PagedMenu;

public class ActionListenerController {

    private static final ActionListenerController instance = new ActionListenerController();
    private OSet<ActionProperties> actionPropertiesOSet = new OConcurrentSet<>();

    private ActionListenerController() {

        // Next Page Handler
        actionPropertiesOSet.add(
                new ActionProperties()
                        .actionId("nextPage")
                        .buttonAction(event -> {
                            if (!(event.getMenu() instanceof PagedMenu)) return;

                            PagedMenu menu = (PagedMenu) event.getMenu();
                            OptionalConsumer<WrappedInventory> nextPage = menu.getNextPage(event.getWrappedInventory());
                            if (nextPage.isPresent())
                                nextPage.get().open(event.getPlayer());

                        })
        );

        // Last Page Handler
        actionPropertiesOSet.add(
                new ActionProperties()
                        .actionId("lastPage")
                        .buttonAction(event -> {
                            if (!(event.getMenu() instanceof PagedMenu)) return;

                            PagedMenu menu = (PagedMenu) event.getMenu();
                            OptionalConsumer<WrappedInventory> previousPage = menu.getPreviousPage(event.getWrappedInventory());
                            if (previousPage.isPresent())
                                previousPage.get().open(event.getPlayer());

                        })
        );

        // Return to child handler
        actionPropertiesOSet.add(
                new ActionProperties()
                        .actionId("return")
                        .buttonAction(event -> {
                            if (event.getMenu().parent() != null)
                                event.getMenu().parent().getWrappedInventory().open(event.getPlayer());
                        })
        );

        // Open a menu from a tree
        actionPropertiesOSet.add(
                new ActionProperties()
                        .actionId("open")
                        .buttonAction(event -> {
                            OptionalConsumer<String> targetMenu = event.getClickedButton().grab("targetMenu", String.class);
                            if (!targetMenu.isPresent()) return;

                            event.getMenu().getChild(targetMenu.get(), true).ifPresent(menu -> menu.getWrappedInventory().open(event.getPlayer()));
                        })
        );
    }

    public void listen(ActionProperties properties) {
        this.actionPropertiesOSet.add(properties);
    }

    public void listen(String actionId, IButtonAction action) {
        ActionProperties properties = new ActionProperties();
        properties.buttonAction(action);
        listen(properties);
    }

    public void listen(String menuId, String actionId, IButtonAction action) {
        ActionProperties properties = new ActionProperties();
        properties.buttonAction(action);
        properties.menuId(menuId);
        properties.actionId(actionId);
        listen(properties);
    }

    public static ActionListenerController getInstance() {
        return instance;
    }
}
