package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.ButtonEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.oop.orangeengine.main.Engine.getEngine;

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
            OptionalConsumer<ActionProperties> properties = OptionalConsumer.of(ActionListenerController.getInstance().getActionPropertiesOSet().stream()
                    .filter(action -> action.actionId() != null && action.actionId().contentEquals(actionId))
                    .findFirst()
                    .orElse(null));

            if (!properties.isPresent()) {
                event.getPlayer().sendMessage(Helper.color("&cError happened! Contact administration!"));
                getEngine().getLogger().printError("Failed to find action listener for menu " + event.getMenu().identifier() + " id " + actionId);

            } else {
                ActionProperties<ButtonEvent> props = properties.get();
                if (props.buttonEventClass().isAssignableFrom(event.getClass()))
                    props.buttonAction().onAction(event);
            }
        });
    }

    public static Map<String, Function<String, Consumer<ButtonClickEvent>>> getActionTypes() {
        return actionTypes;
    }
}