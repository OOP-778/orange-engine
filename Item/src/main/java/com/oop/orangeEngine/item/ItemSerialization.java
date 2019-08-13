package com.oop.orangeEngine.item;

import com.oop.orangeEngine.item.custom.OBanner;
import com.oop.orangeEngine.item.custom.OPotion;
import com.oop.orangeEngine.item.custom.OSkull;
import com.oop.orangeEngine.yaml.mapper.section.ConfigurationSerializable;

import java.util.HashSet;
import java.util.Set;

public class ItemSerialization implements ConfigurationSerializable<ItemBuilder> {

    private static final Set<ItemBuilder> TYPES = new HashSet<ItemBuilder>(){{
        add(new OBanner());
        add(new OPotion());
        add(new OSkull());
    }};



}
