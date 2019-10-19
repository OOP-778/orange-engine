package com.oop.orangeengine.menu.config.button.types;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class ConfigFillerButton extends ConfigNormalButton {
    public ConfigFillerButton(ConfigurationSection section) {
        super(section);
    }

    @Override
    public AMenuButton toButton() {
        if(item() != null)
            item().setDisplayName(" ");
        return super.toButton();
    }
}
