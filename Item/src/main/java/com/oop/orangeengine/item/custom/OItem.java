package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.mapper.section.ConfigurationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OItem extends ItemBuilder<OItem> implements Cloneable {

    public OItem() {
        super(Material.AIR, 1);
    }

    public OItem(Material material) {
        super(material, 1);
    }

    public OItem(OMaterial material) {
        super(material.parseItem());
    }

    public OItem(ItemStack item) {
        super(item);
    }

    @Override
    public OItem clone() {
        try {
            return (OItem) super.clone();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected OItem _returnThis() {
        return this;
    }
}
