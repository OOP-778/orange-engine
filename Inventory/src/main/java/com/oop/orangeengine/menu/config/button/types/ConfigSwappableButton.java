package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.impl.SwappableButton;
import com.oop.orangeengine.menu.events.ButtonItemSwapEvent;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigSwappableButton extends ConfigNormalButton {

    private OItem swapItem;
    public ConfigSwappableButton(ConfigurationSection section) {
        super(section);

        if (section.hasValue("on swap"))
            swapItem = new OItem().load(section);
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
        SwappableButton swappableButton = new SwappableButton(item().getItemStack(), -1);
        if (swapItem != null) {
            swappableButton.toSwap(swapItem.getItemStack());
        }

        return swappableButton;
    }
}
