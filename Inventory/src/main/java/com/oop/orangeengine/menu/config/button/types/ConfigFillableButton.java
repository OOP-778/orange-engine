package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.button.ClickListener;
import com.oop.orangeengine.menu.button.impl.FillableButton;
import com.oop.orangeengine.menu.config.action.ActionTypesController;
import com.oop.orangeengine.menu.events.ButtonEmptyEvent;
import com.oop.orangeengine.menu.events.ButtonFillEvent;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigFillableButton extends ConfigNormalButton {

    public ConfigFillableButton(ConfigurationSection section) {
        super(section);

        if (section.hasChild("on fill")) {
            ConfigurationSection onFillSection = section.getSection("on fill");

            for (String actionType : ActionTypesController.getActionTypes().keySet())
                onFillSection.ifValuePresent(actionType, String.class, text -> {
                    clickListeners().add(new ClickListener<>(ButtonFillEvent.class).clickEnum(ClickEnum.GLOBAL).consumer(ActionTypesController.getActionTypes().get(actionType).apply(text)));
                    appliedActions().add(text);
                });

        }

        if (section.hasChild("on empty")) {
            ConfigurationSection onEmptySection = section.getSection("on empty");

            for (String actionType : ActionTypesController.getActionTypes().keySet())
                onEmptySection.ifValuePresent(actionType, String.class, text -> {
                    clickListeners().add(new ClickListener<>(ButtonEmptyEvent.class).clickEnum(ClickEnum.GLOBAL).consumer(ActionTypesController.getActionTypes().get(actionType).apply(text)));
                    appliedActions().add(text);
                });
        }
    }

    @Override
    public AMenuButton toButton() {

        // Construct button
        constructedButton(privConstructButton());

        // Fill Normal Button Information
        toButton(constructedButton());

        return constructedButton();
    }

    @Override
    public FillableButton privConstructButton() {
        FillableButton button = new FillableButton();
        return button;
    }
}
