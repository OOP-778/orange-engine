package com.oop.orangeengine.item.custom;

import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.mapper.section.ConfigurationSerializable;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OItem extends ItemBuilder implements ConfigurationSerializable<OItem> {

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
    public String getType() {
        return "item";
    }

    @Override
    public OItem load(ConfigurationSection section) {

        OMaterial material = OMaterial.matchMaterial(section.getValueAsReq("material", String.class));
        assert material != null;

        setItemStack(material.parseItem());

        //Load Display name
        section.ifValuePresent("display name", String.class, this::setDisplayName);

        //Load lore
        section.ifValuePresent("lore", List.class, this::setLore);

        // Load amount
        section.ifValuePresent("amount", Integer.class, this::setAmount);

        //Load glow
        section.ifValuePresent("glow", boolean.class, bool -> {
            if(bool)
                makeGlow();
        });

        //Load Enchants
        section.ifValuePresent("enchants", List.class, list -> asListString(list, stringList -> {
            for(String enchant : stringList) {

                String[] split = enchant.split(":");
                if(split.length <= 1) continue;

                addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]));

            }
        }));

        return this;

    }

    @Override
    public void save(ConfigurationSection section, OItem object) {

        // Set material
        section.setValue("material", object.getMaterial().name());

        // Set display name
        if (object.getDisplayName().length() > 0)
            section.setValue("display name", object.getDisplayName());

        // Set if glow
        if (object.isGlow())
            section.setValue("glow", true);

        if (object.getAmount() > 1)
            section.setValue("amount", object.getAmount());

        // Set lore
        if (!object.getLore().isEmpty())
            section.setValue("lore", object.getLore());

        // Set Enchants
        Set<OPair<Enchantment, Integer>> enchants = object.getEnchants();
        if (!enchants.isEmpty())
            section.setValue(
                    "enchants",
                    enchants.stream()
                            .map(enchant -> enchant.getFirst().getName() + ":" + enchant.getSecond())
                            .collect(Collectors.toList())
            );


    }

    @Override
    public String getSectionName(OItem object) {
        return "";
    }

    private void asListString(List list, Consumer<List<String>> consumer) {
        consumer.accept(list);
    }

}
