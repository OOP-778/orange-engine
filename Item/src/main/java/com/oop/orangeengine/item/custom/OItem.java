package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    public OItem(OItem from) {
        super(from.getItemStack().clone());
    }

    @Override
    protected OItem _returnThis() {
        return this;
    }
}
