package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.SwappableButton;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigSwappableButton extends ConfigNormalButton {
    public ConfigSwappableButton(ConfigurationSection section) {
        super(section);
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
    public SwappableButton privConstructButton() {
        return new SwappableButton(item().getItemStack(), -1);
    }
}
