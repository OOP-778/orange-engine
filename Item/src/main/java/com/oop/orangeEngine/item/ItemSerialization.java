package com.oop.orangeEngine.item;

import com.oop.orangeEngine.item.custom.OBanner;
import com.oop.orangeEngine.item.custom.OItem;
import com.oop.orangeEngine.main.component.AEngineComponent;
import com.oop.orangeEngine.yaml.OConfiguration;

public class ItemSerialization extends AEngineComponent {

    public ItemSerialization(){
        super();

        OConfiguration.registerDefaultSerializer(OItem.class);
        OConfiguration.registerDefaultSerializer(OBanner.class);

    }

    @Override
    public String getName() {
        return "Item Serialization";
    }
}
