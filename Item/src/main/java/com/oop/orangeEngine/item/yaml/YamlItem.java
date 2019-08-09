package com.oop.orangeEngine.item.yaml;

import com.oop.orangeEngine.item.OItem;
import com.oop.orangeEngine.material.OMaterial;
import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.OConfiguration;

import java.util.List;

public class YamlItem {

    public static OItem loadItem(OConfiguration configuration, String path) {

        ConfigurationSection section = configuration.createNewSection(path);

        OMaterial material = OMaterial.matchMaterial(section.getValueAsReq("material", String.class));
        OItem item = new OItem(material);

        section.ifValuePresent("display name", String.class, item::setDisplayName);
        section.ifValuePresent("lore", List.class, item::setLore);

        return item;

    }

}
