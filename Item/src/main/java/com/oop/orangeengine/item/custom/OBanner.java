package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class OBanner extends ItemBuilder<OBanner> {
    public OBanner(@NonNull ItemStack item) {
        super(item);
        Helper.assertTrue(OMaterial.matchMaterial(item).name().contains("BANNER"), "Cannot initialize OBanner.class because given itemStack isn't a Banner!");
    }

    public OBanner(@NonNull OMaterial material) {
        super(material);
        Helper.assertTrue(material.name().contains("BANNER"), "Cannot initialize OBanner.class because given material isn't a Banner!");
    }

    public OBanner(OBanner from) {
        super(from.getItemStack().clone());
    }

    @Override
    public BannerMeta getItemMeta() {
        return (BannerMeta) super.getItemMeta();
    }

    @Override
    protected OBanner _returnThis() {
        return this;
    }
}
