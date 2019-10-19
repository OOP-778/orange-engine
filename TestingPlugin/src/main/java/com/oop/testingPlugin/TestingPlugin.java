package com.oop.testingPlugin;

import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.OffPlayerArg;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.ClickListener;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.button.ActionTypesController;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.ButtonFillEvent;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.entity.Player;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        OFile file = new OFile(getDataFolder(), "exampleMenu.yml").createIfNotExists(true);
        OConfiguration configuration = new OConfiguration(file);

        ActionListenerController.getInstance().listen("test", event -> event.getPlayer().sendMessage("tetst"));
        ActionListenerController.getInstance().listen("onFill", ButtonFillEvent.class, event -> event.getPlayer().sendMessage("You've filled with " + event.getFill()));

        ConfigMenuTemplate menuTemplate = new ConfigMenuTemplate(configuration.getSection("menu1"));

        CommandController commandController = new CommandController(this);
        commandController.register(
                new OCommand()
                        .label("openMenu")
                        .description("Testing Command")
                        .listen(command -> {
                            AMenu menu =  menuTemplate.build();
                            Helper.print("Menu: " + menu.getClass());

                            WrappedInventory wrappedInventory = menu.getWrappedInventory();
                            Helper.print("Wrapped Menu: " + wrappedInventory);

                            wrappedInventory.open((Player) command.getSender());
                        })
        );


    }
}

