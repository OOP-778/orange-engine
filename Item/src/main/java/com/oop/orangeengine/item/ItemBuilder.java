package com.oop.orangeengine.item;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.yaml.Typeable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class ItemBuilder implements Typeable, Cloneable {

    @Getter
    @Setter
    private ItemStack itemStack;

    public ItemBuilder(ItemStack item) {
        this.itemStack = item;

        if (item.getItemMeta() != null) {
            if (item.getItemMeta().hasLore())
                setLore(item.getItemMeta().getLore());

            if (item.getItemMeta().hasDisplayName())
                setDisplayName(item.getItemMeta().getDisplayName());
        }
    }

    public ItemBuilder(OMaterial material) {
        this(material.parseItem());
    }

    public ItemBuilder(Material mat, int amount) {
        this(new ItemStack(mat, amount));
    }

    public ItemMeta getItemMeta() {
        if (itemStack.hasItemMeta())
            return itemStack.getItemMeta();

        else {
            itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
            return itemStack.getItemMeta();
        }
    }

    public <T extends ItemMeta> T getItemMeta(Class<T> type) {
        return type.cast(getItemMeta());
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || (lore.size() - 1 < index)) return this;

        lore.remove(index);
        meta.setLore(lore);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder replaceInLore(String key, String value) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.isEmpty()) return this;

        lore.replaceAll(string -> string.replace(key, value));
        meta.setLore(lore);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder makeUnstackable() {
        addNBTTag("_randomness_" + ThreadLocalRandom.current().nextInt(999999), "_randomness_" + ThreadLocalRandom.current().nextInt(999999));
        return this;
    }

    public ItemBuilder addNBTTag(String key, Object value) {
        NBTItem nbt = new NBTItem(itemStack);
        nbt.setObject(key, value);

        itemStack = nbt.getItem();
        return this;
    }

    public ItemBuilder setLoreLine(int index, String text) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || ((lore.size() - 1) < index))
            return appendLore(text);

        lore.set(index, Helper.color(text));
        meta.setLore(lore);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder appendLore(String text) {
        ItemMeta meta = getItemMeta();
        List<String> lore = getLore();

        lore.add(Helper.color(text));
        meta.setLore(lore);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDisplayName(String displayName) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setDisplayName(Helper.color(displayName));

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setLore(
                    lore.stream()
                            .map(Helper::color)
                            .collect(Collectors.toList())
            );

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flags);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        meta.removeItemFlags(flags);

        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchant, int level) {
        itemStack.addUnsafeEnchantment(enchant, level);
        return this;
    }

    public ItemBuilder setDurability(int durability) {
        return setDurability((byte) durability);
    }

    public ItemBuilder setDurability(byte durability) {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder makeGlow() {
        return addNBTTag("ench", "OrangeEngine");
    }

    public ItemBuilder replaceDisplayName(String key, String value) {
        setDisplayName(getDisplayName().replace(key, value));
        return this;
    }

    public String getDisplayName() {
        if (getItemMeta() == null)
            return "";

        return getItemMeta().hasDisplayName() ? getItemMeta().getDisplayName() : "";
    }

    public List<String> getLore() {
        ItemMeta meta = getItemMeta();
        return meta.hasLore() ? meta.getLore() : new ArrayList<>();
    }

    public Set<OPair<Enchantment, Integer>> getEnchants() {
        return itemStack.getEnchantments().entrySet().stream()
                .map(k -> new OPair<>(k.getKey(), k.getValue()))
                .collect(Collectors.toSet());
    }

    public boolean isGlow() {
        NBTItem nbt = new NBTItem(itemStack);
        return nbt.hasKey("ench");
    }

    public ItemBuilder setMaterial(Material material) {
        itemStack.setType(material);
        return this;
    }

    public Material getMaterial() {
        return itemStack.getType();
    }

    @Deprecated
    public ItemBuilder addLore(String text) {
        return appendLore(text);
    }

    public ItemBuilder setAmount(int amount) {
        getItemStack().setAmount(amount);
        return this;
    }

    public int getAmount() {
        return getItemStack().getAmount();
    }

    public boolean hasNBTTag(String key) {
        return new NBTItem(getItemStack()).hasKey(key);
    }

    public Object getNBTTag(String key) {
        return new NBTItem(getItemStack()).getObject(key, Object.class);
    }

    public <T> T getNBTTag(String key, Class<T> type) {
        return new NBTItem(getItemStack()).getObject(key, type);
    }

    public ItemBuilder mergeLore(List<String> secondLore) {
        secondLore.forEach(getLore()::add);
        return this;
    }

}
