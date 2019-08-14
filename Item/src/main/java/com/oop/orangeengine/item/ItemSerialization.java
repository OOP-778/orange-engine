package com.oop.orangeengine.item;

import com.oop.orangeengine.item.custom.OBanner;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.yaml.OConfiguration;

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
