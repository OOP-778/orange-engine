package com.oop.orangeengine.menu.config.button;

import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.events.ButtonClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActionTypesController {

    private static Map<String, Function<String, Consumer<ButtonClickEvent>>> actionTypes = new HashMap<>();
    static {
        actionTypes.put("open", menuName -> event -> {

            event.getMenu().getChild(menuName, true).ifPresentOrElse(
                    menu -> {
                        menu.getWrappedInventory().open(event.getPlayer());
                    },
                    () -> {
                        throw new IllegalStateException("Failed to open child of " + event.getMenu().identifier() + " child " + menuName + ". Because child is not found!");
                    });
        });

        actionTypes.put("execute action", actionId -> event -> {
            OptionalConsumer.of(ActionListenerController.getInstance().getActionPropertiesOSet().stream()
                    .filter(action -> action.actionId() != null && action.actionId().equalsIgnoreCase(actionId))
                    .findFirst()
            ).
                    ifPresentOrElse(
                            action -> action.buttonAction().onAction(event),
                            () -> {
                                throw new IllegalStateException("Failed to find action listener for menu " + event.getMenu().identifier() + " id " + actionId);
                            }
                    );
        });
    }

    public static Map<String, Function<String, Consumer<ButtonClickEvent>>> getActionTypes() {
        return actionTypes;
    }
}
