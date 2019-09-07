package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.button.impl.OButton;
import com.oop.orangeengine.menu.config.button.AConfigButton;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigNormalButton extends AConfigButton {
    public ConfigNormalButton(ConfigurationSection section) {
        super(section);
    }

    @Override
    public AMenuButton toButton() {
        constructedButton(privConstructButton());
        toButton(constructedButton());

        return constructedButton();
    }

    @Override
    public AMenuButton privConstructButton() {
        return new OButton(item().getItemStack(), -1);
    }

    protected void toButton(AMenuButton button) {

        // Set click handler
        for (ClickEnum clickEnum : clickHandler().keySet())
            button.clickHandler().put(clickEnum, clickHandler().get(clickEnum));


        // Set sound


    }
}
