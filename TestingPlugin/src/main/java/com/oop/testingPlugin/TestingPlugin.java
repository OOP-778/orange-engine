package com.oop.testingPlugin;

import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.task.ClassicTaskController;
import com.oop.orangeengine.main.task.ITaskController;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.MenuDesigner;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.ClickListener;
import com.oop.orangeengine.menu.button.impl.OButton;
import com.oop.orangeengine.menu.button.impl.SwappableButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.config.action.ActionTypesController;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.ButtonFillEvent;
import com.oop.orangeengine.menu.types.PagedMenu;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {
        OFile file = new OFile(getDataFolder(), "exampleMenu.yml").createIfNotExists(true);
        OConfiguration configuration = new OConfiguration(file);

        PagedMenu menu = new PagedMenu("something", 3);
        MenuDesigner designer = new MenuDesigner(new ArrayList<String>(){{
            add("YYYYYYYYY");
            add("XXXXXXXXX");
            add("YYYLYNYYY");
        }});

        SwappableButton nextPage = new SwappableButton(
                new OItem(OMaterial.ARROW)
                        .setDisplayName("&a&lNext page")
                        .getItemStack(), -1
        );
        nextPage.addClickHandler(new ClickListener<ButtonClickEvent>(ButtonClickEvent.class).consumer(ActionTypesController.getActionTypes().get("execute action").apply("next page")));
        nextPage.appliedActions().add("next page");
        nextPage.toSwap(OMaterial.YELLOW_STAINED_GLASS_PANE.parseItem());

        SwappableButton lastPage = new SwappableButton(
                new OItem(OMaterial.ARROW)
                        .setDisplayName("&bLast page")
                        .getItemStack(), -1
        );
        lastPage.addClickHandler(new ClickListener<ButtonClickEvent>(ButtonClickEvent.class).consumer(ActionTypesController.getActionTypes().get("execute action").apply("last page")));
        lastPage.appliedActions().add("last page");
        lastPage.toSwap(OMaterial.YELLOW_STAINED_GLASS_PANE.parseItem());

        designer.setButton('N', nextPage);
        designer.setButton('L', lastPage);

        designer.setButton('Y', new OButton(
                new OItem(OMaterial.AIR)
                .getItemStack()
                , -1
        ).actAsFilled(true));
        menu.designer(designer);

        for (int i = 0; i < 100; i++)
            menu.addButton(new OButton(new OItem(OMaterial.ANDESITE).makeUnstackable().getItemStack(), -1).paged(true));

        CommandController cmdController = new CommandController(this);
        cmdController.register(
                new OCommand().
                        label("testingMenu")
                        .ableToExecute(Player.class)
                        .onCommand(cmd -> {
                            menu.getWrappedInventory().open((Player) cmd.getSender());
                        })
        );

    }

    @Override
    public ITaskController provideTaskController() {
        return new ClassicTaskController(this);
    }
}

