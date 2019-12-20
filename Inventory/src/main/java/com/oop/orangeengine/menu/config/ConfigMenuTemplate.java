package com.oop.orangeengine.menu.config;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.MenuDesigner;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.config.button.AConfigButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.types.BasicMenu;
import com.oop.orangeengine.menu.types.PagedMenu;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.oop.orangeengine.main.Helper.assertTrue;

@Getter
public class ConfigMenuTemplate {

    private String menuIdentifier;
    private MenuType menuType;
    private String title;

    private MenuDesigner designer;

    private List<AConfigButton> buttons = new ArrayList<>();
    private Map<String, ConfigMenuTemplate> children = Maps.newHashMap();

    public ConfigMenuTemplate(ConfigurationSection configuration) {
        this.menuIdentifier = configuration.getKey();

        // Set menu type
        Helper.assertTrue(configuration.hasValue("type"), "Failed to find menu type in " + configuration.getKey());
        menuType = MenuType.valueOf(configuration.getValueAsReq("type").toString().toUpperCase());

        // Set title if found
        configuration.ifValuePresent("title", String.class, title -> this.title = title);

        // Set designer layout
        assert configuration.hasValue("layout");
        designer = new MenuDesigner(configuration.getValueAsReq("layout"));

        // Init buttons
        if (configuration.hasChild("buttons")) {
            ConfigurationSection buttonsSection = configuration.getSection("buttons");

            for (ConfigurationSection buttonSection : buttonsSection.getSections().values()) {
                AConfigButton configButton = AConfigButton.fromConfig(buttonSection);
                assertTrue( configButton != null, "Failed to initialize button id: " + buttonSection.getKey() + ", type: " + buttonSection.getValueAsReq("type"));

                if (!configButton.placeholder()) {
                    designer.setButton(configButton.layoutId().toCharArray()[0], configButton.toButton());

                } else
                    buttons.add(configButton);
            }
        }

        // Init children menus
        if (configuration.hasChild("children"))
            configuration.getSection("children").getSections().values().forEach(child -> children.put(child.getKey(), new ConfigMenuTemplate(child)));
    }

    public AMenu build(boolean withChildren) {
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
        if (withChildren)
            children.values().forEach(configMenu -> menu.addChild(configMenu.build(withChildren)));

        return menu;
    }

    public AMenu build() {
        return build(true);
    }

    private AMenu getMenu() {
        if (menuType == MenuType.BASIC)
            return new BasicMenu(menuIdentifier, designer.getSize()).designer(designer.clone());

        else
            return new PagedMenu(menuIdentifier, designer.getSize()).designer(designer.clone());
    }

    public void getAllChildren(List<ConfigMenuTemplate> list) {
        list.add(this);
        getChildren().values().forEach(menu -> menu.getAllChildren(list));
    }

    public <T extends ButtonClickEvent> void listen(ActionProperties<T> props) {
        ActionListenerController.getInstance().listen(props.menuId(getMenuIdentifier()));
    }

}