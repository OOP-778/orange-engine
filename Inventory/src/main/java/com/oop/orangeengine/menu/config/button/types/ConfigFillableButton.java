package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.FillableButton;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigFillableButton extends ConfigNormalButton {

    public ConfigFillableButton(ConfigurationSection section) {
        super(section);

        String onFill, onEmpty;
        if (section.hasChild("on fill")) {

        }

        if (section.hasChild("on empty")) {

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
