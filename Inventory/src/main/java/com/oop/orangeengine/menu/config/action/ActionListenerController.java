package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import com.oop.orangeengine.main.util.data.set.OSet;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.types.PagedMenu;
import lombok.Getter;

public class ActionListenerController {

    private static final ActionListenerController instance = new ActionListenerController();

    @Getter
    private OSet<ActionProperties> actionPropertiesOSet = new OConcurrentSet<>();

    private ActionListenerController() {

        // Next Page Handler
        actionPropertiesOSet.add(
                new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class)
                        .actionId("next page")
                        .buttonAction(event -> {
                            if (!(event.getMenu() instanceof PagedMenu)) return;

                            System.out.println("RUnning next page");
                            PagedMenu menu = (PagedMenu) event.getMenu();
                            OptionalConsumer<WrappedInventory> nextPage = menu.getNextPage(event.getWrappedInventory());
                            if (nextPage.isPresent())
                                nextPage.get().open(event.getPlayer());

                        })
        );

        // Last Page Handler
        actionPropertiesOSet.add(
                new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class)
                        .actionId("previous page")
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
                new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class)
                        .actionId("return")
                        .buttonAction(event -> {
                            if (event.getMenu().parent() != null)
                                event.getMenu().parent().getWrappedInventory().open(event.getPlayer());

                            else
                                event.getPlayer().sendMessage(Helper.color("&cThe parent is not found!"));
                        })
        );
    }

    public ActionListenerController listen(ActionProperties properties) {
        this.actionPropertiesOSet.add(properties);
        return this;
    }

    public void listen(String actionId, IButtonAction<ButtonClickEvent> action) {
        ActionProperties<ButtonClickEvent> properties = new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class);
        properties.actionId(actionId);
        properties.buttonAction(action);
        listen(properties);
    }

    public <T extends ButtonClickEvent> void listen(String actionId, Class<T> klass, IButtonAction<T> action) {
        ActionProperties<T> properties = new ActionProperties<>(klass);
        properties.actionId(actionId);
        properties.buttonAction(action);
        listen(properties);
    }

    public void listen(String menuId, String actionId, IButtonAction<ButtonClickEvent> action) {
        ActionProperties<ButtonClickEvent> properties = new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class);
        properties.buttonAction(action);
        properties.menuId(menuId);
        properties.actionId(actionId);
        listen(properties);
    }

    public static ActionListenerController getInstance() {
        return instance;
    }
}
