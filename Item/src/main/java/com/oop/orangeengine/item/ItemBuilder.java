package com.oop.orangeengine.item;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.item.custom.OPotion;
import com.oop.orangeengine.item.custom.OSkull;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ItemBuilder<T extends ItemBuilder> implements Cloneable {

    @Getter
    @Setter
    private ItemStack itemStack;

    public ItemBuilder(@NonNull ItemStack item) {
        this.itemStack = item;
    }

    public ItemBuilder(@NonNull OMaterial material) {
        this(material.parseItem());
    }

    public ItemBuilder(@NonNull Material mat, int amount) {
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

    public T itemMeta(ItemMeta meta) {
        itemStack.setItemMeta(meta);
        return _returnThis();
    }

    public T removeLoreLine(int index) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || (lore.size() - 1 < index)) return _returnThis();

        lore.remove(index);
        meta.setLore(lore);

        itemMeta(meta);
        return _returnThis();
    }

    public T replaceInLore(String key, String value) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.isEmpty()) return _returnThis();

        lore.replaceAll(string -> string.replace(key, value));
        meta.setLore(lore);

        itemMeta(meta);
        return _returnThis();
    }

    public T makeUnstackable() {
        addNBTTag("_randomness_" + ThreadLocalRandom.current().nextInt(999999), "_randomness_" + ThreadLocalRandom.current().nextInt(999999));
        return _returnThis();
    }

    public T addNBTTag(String key, Object value) {
        NBTItem nbt = new NBTItem(itemStack);
        nbt.setObject(key, value);

        itemStack = nbt.getItem();
        return _returnThis();
    }

    public T setLoreLine(int index, String text) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || ((lore.size() - 1) < index))
            return appendLore(text);

        lore.set(index, Helper.color(text));
        meta.setLore(lore);

        itemMeta(meta);
        return _returnThis();
    }

    public T appendLore(String text) {
        ItemMeta meta = getItemMeta();
        List<String> lore = getLore();

        lore.add(Helper.color(text));
        meta.setLore(lore);

        itemMeta(meta);
        return _returnThis();
    }

    public T setDisplayName(String displayName) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setDisplayName(Helper.color(displayName));

        itemMeta(meta);
        return _returnThis();
    }

    public T setLore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setLore(
                    lore.stream()
                            .map(Helper::color)
                            .collect(Collectors.toList())
            );

        itemMeta(meta);
        return _returnThis();
    }

    public T addItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flags);

        itemMeta(meta);
        return _returnThis();
    }

    public T removeItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        meta.removeItemFlags(flags);

        itemMeta(meta);
        return _returnThis();
    }

    public T addEnchant(Enchantment enchant, int level) {
        itemStack.addUnsafeEnchantment(enchant, level);
        return _returnThis();
    }

    public T setDurability(int durability) {
        return setDurability((byte) durability);
    }

    public T setDurability(byte durability) {
        itemStack.setDurability(durability);
        return _returnThis();
    }

    public T makeGlow() {
        return addNBTTag("ench", "OrangeEngine");
    }

    public T replaceDisplayName(String key, String value) {
        setDisplayName(getDisplayName().replace(key, value));
        return _returnThis();
    }

    public String getDisplayName() {
        if (getItemMeta() == null)
            return "";

        return getItemMeta().hasDisplayName() ? getItemMeta().getDisplayName() : "";
    }

    public List<String> getLore() {
        ItemMeta meta = getItemMeta();
        return (meta == null || !meta.hasLore()) ? new ArrayList<>() : meta.getLore();
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

    public T setMaterial(Material material) {
        itemStack.setType(material);
        return _returnThis();
    }

    public T setMaterial(OMaterial material) {
        itemStack.setType(material.parseMaterial());
        itemStack.setDurability(material.getData());
        return _returnThis();
    }

    public Material getMaterial() {
        return itemStack.getType();
    }

    public OMaterial getOMaterial() {
        return OMaterial.matchMaterial(getItemStack());
    }

    @Deprecated
    public T addLore(String text) {
        return appendLore(text);
    }

    public T setAmount(int amount) {
        getItemStack().setAmount(amount);
        return _returnThis();
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

    public T mergeLore(List<String> secondLore) {
        List<String> lore = getLore();
        lore.addAll(secondLore);
        setLore(lore);
        return _returnThis();
    }

    public T mergeLore(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) return _returnThis();
        return mergeLore(itemStack.getItemMeta().getLore());
    }

    public T load(ConfigurationSection section) {
        OMaterial material = OMaterial.matchMaterial(section.getValueAsReq("material", String.class));
        Objects.requireNonNull(material, "Failed to find material by " + section.getValueAsReq("material"));

        setItemStack(material.parseItem());
        if (getItemStack().getItemMeta() == null)
            getItemStack().setItemMeta(Bukkit.getItemFactory().getItemMeta(material.parseMaterial()));

        //Load Display name
        section.ifValuePresent("display name", String.class, this::setDisplayName);

        //Load lore
        section.ifValuePresent("lore", List.class, this::setLore);

        // Load amount
        section.ifValuePresent("amount", Integer.class, this::setAmount);

        //Load glow
        section.ifValuePresent("glow", boolean.class, bool -> {
            if (bool)
                makeGlow();
        });

        //Load Enchants
        section.ifValuePresent("enchants", List.class, list -> asListString(list, stringList -> {
            for (String enchant : stringList) {

                String[] split = enchant.split(":");
                if (split.length <= 1) continue;

                addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]));

            }
        }));

        return _returnThis();
    }

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

    private void asListString(List list, Consumer<List<String>> consumer) {
        consumer.accept(list);
    }

    protected abstract T _returnThis();

    public static <T extends ItemBuilder> ItemBuilder<T> fromConfiguration(ConfigurationSection section) {
        OMaterial material = OMaterial.matchMaterial(section.getValueAsReq("material", String.class));
        Objects.requireNonNull(material, "Failed to find material by " + section.getValueAsReq("material"));

        if (material.name().contains("HEAD"))
            return (ItemBuilder<T>) new OSkull().load(section);

        else if (material.name().contains("POTION"))
            return (ItemBuilder<T>) new OPotion(material);

        return (ItemBuilder<T>) new OItem().load(section);
    }

    public static <T extends ItemBuilder> ItemBuilder<T> fromItem(@NonNull ItemStack item) {
        OMaterial material = OMaterial.matchMaterial(item);

        if (material.name().contains("HEAD"))
            return (ItemBuilder<T>) new OSkull(item);

        else if (material.name().contains("POTION"))
            return (ItemBuilder<T>) new OPotion(item);

        return (ItemBuilder<T>) new OItem(item);
    }

    public T clone() {
        try {
            return (T) getClass().getConstructor(ItemStack.class).newInstance(itemStack.clone());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
