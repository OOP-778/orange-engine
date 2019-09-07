package com.oop.orangeengine.menu.config.button;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.InventoryController;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.button.ClickEnum;
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
import java.util.Map;
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

    private Map<ClickEnum, Consumer<ButtonClickEvent>> clickHandler = new HashMap<>();

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

            // Open action
            onClickSection.ifValuePresent("open", String.class, menuName -> clickHandler.put(ClickEnum.GLOBAL, event -> {
                event.getMenu().getChild(menuName, true).ifPresentOrElse(
                        (menu) -> {
                            WrappedInventory inventory = menu.getWrappedInventory();
                            if (inventory == null)
                                throw new IllegalStateException("Menu by identifier " + menuName + " failed to provide an Inventory.");

                            event.getPlayer().openInventory(inventory.getBukkitInventory());
                        },
                        () -> event.getPlayer().sendMessage(Helper.color("&cError! Failed to find menu by name " + menuName + " please contact administrator!"))
                );
            }));

            // Execute Action
            onClickSection.ifValuePresent("execute action", String.class, actionIdentifier -> getEngine().findComponentByClass(InventoryController.class).findClickHandler(actionIdentifier, null).ifPresentOrElse(
                    (consumer) -> clickHandler.put(ClickEnum.GLOBAL, consumer),
                    () -> {
                        throw new IllegalStateException("Failed to find action handler by (" + onClickSection.getPath() + "= " + actionIdentifier + ")");
                    }
            ));
        }

        for (ClickEnum clickEnum : ClickEnum.values()) {

            String normalized = "on " + clickEnum.name().replace("_", " ").toLowerCase() + " click";
            if (section.hasChild(normalized)) {
                ConfigurationSection onClickSection = section.getSection(normalized);

                // Open action
                onClickSection.ifValuePresent("open", String.class, menuName -> clickHandler.put(ClickEnum.GLOBAL, event -> {
                    event.getMenu().getChild(menuName, true).ifPresentOrElse(
                            (menu) -> {
                                WrappedInventory inventory = menu.getWrappedInventory();
                                if (inventory == null)
                                    throw new IllegalStateException("Menu by identifier " + menuName + " failed to provide an Inventory.");

                                event.getPlayer().openInventory(inventory.getBukkitInventory());
                            },
                            () -> event.getPlayer().sendMessage(Helper.color("&cError! Failed to find menu by name " + menuName + " please contact administrator!"))
                    );
                }));

                // Execute Action
                onClickSection.ifValuePresent("execute action", String.class, actionIdentifier -> getEngine().findComponentByClass(InventoryController.class).findClickHandler(actionIdentifier, null).ifPresentOrElse(
                        (consumer) -> clickHandler.put(ClickEnum.GLOBAL, consumer),
                        () -> {
                            throw new IllegalStateException("Failed to find action handler by (" + onClickSection.getPath() + "= " + actionIdentifier + ")");
                        }
                ));
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
