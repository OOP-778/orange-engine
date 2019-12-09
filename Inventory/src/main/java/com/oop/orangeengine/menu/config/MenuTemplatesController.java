package com.oop.orangeengine.menu.config;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.config.button.AConfigButton;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class MenuTemplatesController {

    private Map<String, ConfigMenuTemplate> templateMap = Maps.newHashMap();
    private Map<String, AConfigButton> globalButtons = Maps.newHashMap();

    public MenuTemplatesController(OConfiguration configuration) {
        configuration.getSections().values().forEach(section -> {
            if (section.getKey().contentEquals("buttons") || section.getKey().contentEquals("global buttons"))
                loadButtons(section);

            else
                templateMap.put(section.getKey(), new ConfigMenuTemplate(section));
        });

        // Assign global buttons
        getAllTemplates().forEach(menu -> {
            globalButtons.forEach((key, button) -> {
                if (key.length() == 1)
                    menu.getDesigner().setButton(key.toCharArray()[0], button.toButton());

                else
                    menu.getButtons().add(button);
            });
        });
    }

    private List<ConfigMenuTemplate> getAllTemplates() {
        List<ConfigMenuTemplate> templates = new ArrayList<>();
        templateMap.values().forEach(menu -> menu.getAllChildren(templates));

        return templates;
    }

    private void loadButtons(ConfigurationSection section) {
        section.getSections().values().forEach(buttonSection -> globalButtons.put(buttonSection.getKey(), AConfigButton.fromConfig(buttonSection)));
    }

    public OptionalConsumer<ConfigMenuTemplate> findTemplateById(String id) {
        return OptionalConsumer.of(templateMap.get(id));
    }

}
