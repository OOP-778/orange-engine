package com.oop.orangeengine.menu.config;

import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.MenuDesigner;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.config.button.AConfigButton;
import com.oop.orangeengine.menu.types.BasicMenu;
import com.oop.orangeengine.menu.types.PagedMenu;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigMenuTemplate {

    private String menuIdentifier;
    private MenuType menuType;
    private String title;

    private List<String> layout = new ArrayList<>();
    private List<AConfigButton> buttons = new ArrayList<>();

    private List<ConfigMenuTemplate> children = new ArrayList<>();

    public ConfigMenuTemplate(OConfiguration configuration) {

        assert configuration.hasValue("type");


        menuType = MenuType.valueOf(configuration.getValueAsReq("type").toString().toUpperCase());


    }

    public AMenu build() {
        AMenu menu = getMenu();

        // Set title
        menu.title(title);

        // Apply actions to menu
        ActionListenerController.getInstance().getActionPropertiesOSet().stream()
                .filter(props -> props.accepts(this))
                .forEach(props -> menu.actionSet().add(props));

        // Build all Buttons
        buttons.forEach(button -> menu.addButton(button.toButton()));

        // Build children
        children.forEach(configMenu -> menu.addChild(configMenu.build()));

        return menu;
    }

    private AMenu getMenu() {

        MenuDesigner menuDesigner = new MenuDesigner(layout);
        if (menuType == MenuType.BASIC)
            return new BasicMenu(menuIdentifier, menuDesigner.getSize()).designer(menuDesigner);

        else
            return new PagedMenu(menuIdentifier, menuDesigner.getSize()).designer(menuDesigner);
    }
}