package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public class OPotion extends ItemBuilder<OPotion> {

    public OPotion(@NonNull ItemStack item) {
        super(item);
    }

    public OPotion(@NonNull OMaterial material) {
        super(material);
    }

    public OPotion(@NonNull Material mat, int amount) {
        super(mat, amount);
    }

    public OPotion(OPotion from) {
        super(from.getItemStack().clone());
    }

    @Override
    public PotionMeta getItemMeta() {
        return (PotionMeta) super.getItemMeta();
    }

    public OPotion addEffect(PotionEffect effect, boolean b) {
        PotionMeta itemMeta = getItemMeta();
        if (itemMeta.hasCustomEffect(effect.getType())) return this;

        itemMeta.addCustomEffect(effect, b);
        return this;
    }
}
