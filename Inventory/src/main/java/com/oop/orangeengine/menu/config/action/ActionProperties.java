package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Predicate;

@Data
@Accessors(chain = true, fluent = true)
public class ActionProperties {

    private String menuId;
    private ClickEnum clickEnum;
    private String actionId;
    private IButtonAction buttonAction;
    private Predicate<AMenuButton> customFilter;

    public boolean accepts(ButtonClickEvent event) {
        if (menuId != null) {
            OptionalConsumer<String> menuId = event.getMenu().grab("menuId", String.class);
            if (!menuId.isPresent() || !menuId.get().equalsIgnoreCase(this.menuId))
                return false;
        }

        if (clickEnum != null && event.getClickType() != clickEnum)
            return false;

        if (customFilter != null && !customFilter.test(event.getClickedButton()))
            return false;

        if (actionId != null) {
            OptionalConsumer<String> optionalActionId = event.getClickedButton().grab("actionId", String.class);
            if (!optionalActionId.isPresent())
                return false;

            return optionalActionId.get().equalsIgnoreCase(actionId);
        }

        return true;
    }
}
