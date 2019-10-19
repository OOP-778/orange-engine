package com.oop.orangeengine.menu.config.button;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.InventoryController;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
import com.oop.orangeengine.menu.button.ClickListener;
import com.oop.orangeengine.menu.config.button.types.ConfigFillerButton;
import com.oop.orangeengine.menu.config.button.types.ConfigNormalButton;
import com.oop.orangeengine.menu.config.button.types.ConfigSwappableButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.sound.OSound;
import com.oop.orangeengine.sound.WrappedSound;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.oop.orangeengine.main.Engine.getEngine;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
public abstract class AConfigButton {

    private ButtonType type;
    private String layoutId;
    private OItem item;
    private boolean template = false;
    private boolean placeholder = false;
    private WrappedSound sound;

    private Set<ClickListener> clickListeners = new HashSet<>();

    private AMenuButton constructedButton;

    public AConfigButton(ConfigurationSection section) {
        // Set layout Id
        if (section.getKey().length() == 1)
            layoutId = section.getKey();

        else {
            placeholder = true;
            layoutId = section.getKey();
        }

        // Load item
        item = new OItem().load(section);

        // Init button clicking
        if (section.hasChild("on click")) {
            ConfigurationSection onClickSection = section.getSection("on click");

            for (String actionType : ActionTypesController.getActionTypes().keySet())
                onClickSection.ifValuePresent(actionType, String.class, text -> clickListeners.add(new ClickListener<>(ButtonClickEvent.class).clickEnum(ClickEnum.GLOBAL).consumer(ActionTypesController.getActionTypes().get(actionType).apply(text))));

        }

        for (ClickEnum clickEnum : ClickEnum.values()) {

            String normalized = "on " + clickEnum.name().replace("_", " ").toLowerCase() + " click";
            if (section.hasChild(normalized)) {
                ConfigurationSection onClickSection = section.getSection(normalized);

                for (String actionType : ActionTypesController.getActionTypes().keySet())
                    onClickSection.ifValuePresent(actionType, String.class, text -> clickListeners.add(new ClickListener<>(ButtonClickEvent.class).clickEnum(clickEnum).consumer(ActionTypesController.getActionTypes().get(actionType).apply(text))));

            }
        }

        if (section.hasValue("sound"))
            WrappedSound.of(OSound.match(section.getValueAsReq("sound")), 0f, 50f);
    }

    public static AConfigButton fromConfig(ConfigurationSection section) {
        ButtonType type = ButtonType.valueOf(section.getValueAsReq("type").toString().toUpperCase());
        if (type == ButtonType.NORMAL)
            return new ConfigNormalButton(section);

        else if (type == ButtonType.SWAPPABLE)
            return new ConfigSwappableButton(section);

        else if (type == ButtonType.FILLER)
            return new ConfigFillerButton(section);

        return null;
    }

    public abstract AMenuButton toButton();

    public abstract AMenuButton privConstructButton();

}
