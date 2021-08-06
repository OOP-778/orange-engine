package com.oop.orangeengine.item;

import com.google.gson.internal.Primitives;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.item.custom.OPotion;
import com.oop.orangeengine.item.custom.OSkull;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ItemBuilder<T extends ItemBuilder> implements Cloneable {
    private static GlowEnchant glowEnchant;

    static {
        if (OVersion.is(8)) {
            glowEnchant = new GlowEnchant(69);
            try {
                Field byIdField = Enchantment.class.getDeclaredField("byId");
                byIdField.setAccessible(true);

                Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
                acceptingNew.setAccessible(true);
                acceptingNew.setBoolean(null, true);

                Field byNameField = Enchantment.class.getDeclaredField("byName");
                byNameField.setAccessible(true);

                HashMap<Integer, Enchantment> o = (HashMap<Integer, Enchantment>) byIdField.get(null);
                HashMap<String, Enchantment> o2 = (HashMap<String, Enchantment>) byNameField.get(null);

                o.remove(glowEnchant.getId());
                o2.remove(glowEnchant.getName());

            } catch (Exception ex) {
                Engine.getEngine().getOwning().getOLogger().printWarning("Failed to unregister glow enchant!");
            }
            GlowEnchant.registerEnchantment(glowEnchant);
        } else {
            glowEnchant = null;
        }
    }

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

    private ItemMeta meta;
    private NBTItem nbt;

    public ItemMeta getItemMeta() {
        if (meta == null)
            if (itemStack.hasItemMeta())
                meta = itemStack.getItemMeta();
            else
                meta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        return meta;
    }

    public <T extends ItemMeta> T getItemMeta(Class<T> type) {
        return type.cast(getItemMeta());
    }

    public T removeLoreLine(int index) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || (lore.size() - 1 < index)) return (T) this;

        lore.remove(index);
        meta.setLore(lore);
        return (T) this;
    }

    public T replaceInLore(String key, String value) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.isEmpty()) return (T) this;

        lore.replaceAll(string -> string.replace(key, value));
        meta.setLore(lore);

        return (T) this;
    }

    public T makeUnstackable() {
        addNBTTag("_randomness_" + ThreadLocalRandom.current().nextInt(999999), "_randomness_" + ThreadLocalRandom.current().nextInt(999999));
        return (T) this;
    }

    public T addNBTTag(String key, Object value) {
        if (value instanceof String)
            getNbt().setString(key, (String) value);

        else if (Primitives.wrap(value.getClass()) == Integer.class)
            getNbt().setInteger(key, (Integer) value);

        else if (Primitives.wrap(value.getClass()) == Double.class)
            getNbt().setDouble(key, (Double) value);

        else if (Primitives.wrap(value.getClass()) == Long.class)
            getNbt().setLong(key, (Long) value);

        else
            getNbt().setObject(key, value);

        return (T) this;
    }

    public T setLoreLine(int index, String text) {
        ItemMeta meta = getItemMeta();

        List<String> lore = getLore();
        if (lore.size() == 0 || ((lore.size() - 1) < index))
            return appendLore(text);

        lore.set(index, Helper.color(text));
        meta.setLore(lore);

        return (T) this;
    }

    public T appendLore(String text) {
        ItemMeta meta = getItemMeta();
        List<String> lore = getLore();

        lore.add(Helper.color(text));
        meta.setLore(lore);

        return (T) this;
    }

    public T setDisplayName(String displayName) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setDisplayName(Helper.color(displayName));

        return (T) this;
    }

    public T setLore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        if (meta != null)
            meta.setLore(
                    lore.stream()
                            .map(Helper::color)
                            .collect(Collectors.toList())
            );

        return (T) this;
    }

    public T addItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        if (meta == null) return (T) this;

        meta.addItemFlags(flags);

        return (T) this;
    }

    public T removeItemFlag(ItemFlag... flags) {
        ItemMeta meta = getItemMeta();
        meta.removeItemFlags(flags);
        return (T) this;
    }

    public T addEnchant(Enchantment enchant, int level) {
        getItemMeta().addEnchant(enchant, level, true);
        return (T) this;
    }

    public T setDurability(int durability) {
        return setDurability((byte) durability);
    }

    public T setDurability(byte durability) {
        itemStack.setDurability(durability);
        return (T) this;
    }

    public T makeGlow() {
        if (!getEnchants().isEmpty()) return (T) this;
        if (OVersion.isAfter(8)) {
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
            addEnchant(Enchantment.DAMAGE_ALL, 1);

        } else
            addEnchant(glowEnchant, 1);
        return (T) this;
    }

    public T replaceDisplayName(String key, String value) {
        setDisplayName(getDisplayName().replace(key, value));
        return (T) this;
    }

    public T replace(String key, Object value) {
        replaceDisplayName(key, value.toString());
        replaceInLore(key, value.toString());
        return (T) this;
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
        return getNbt().hasKey("ench");
    }

    public T setMaterial(Material material) {
        itemStack.setType(material);
        return (T) this;
    }

    public T setMaterial(OMaterial material) {
        itemStack.setType(material.parseMaterial());
        itemStack.setDurability(material.getData());
        return (T) this;
    }

    public Material getMaterial() {
        return itemStack.getType();
    }

    public OMaterial getOMaterial() {
        return OMaterial.matchMaterial(itemStack);
    }

    @Deprecated
    public T addLore(String text) {
        return appendLore(text);
    }

    public T setAmount(int amount) {
        itemStack.setAmount(amount);
        return (T) this;
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public boolean hasNBTTag(String key) {
        return getNbt().hasKey(key);
    }

    public Object getNBTTag(String key) {
        return getNbt().getObject(key, Object.class);
    }

    public <T> T getNBTTag(String key, Class<T> type) {
        return getNbt().getObject(key, type);
    }

    public T mergeLore(List<String> secondLore) {
        List<String> lore = getLore();
        lore.addAll(secondLore);
        setLore(lore);
        return (T) this;
    }

    public T mergeLore(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) return (T) this;
        return mergeLore(itemStack.getItemMeta().getLore());
    }

    public ItemStack getItemStack() {
        if (meta != null)
            itemStack.setItemMeta(meta);

        if (nbt != null) {
            NBTItem newNbt = nbt;
            NBTItem oldNbt = new NBTItem(itemStack);

            newNbt.removeKey("ench");
            newNbt.removeKey("display");
            oldNbt.mergeCompound(newNbt);

            itemStack = oldNbt.getItem();
        }
        return itemStack;
    }

    public T load(ConfigSection section) {
        OMaterial material = OMaterial.matchMaterial(section.getAs("material", String.class)).get();
        Objects.requireNonNull(material, "Failed to find material by " + section.getAs("material", String.class));

        setItemStack(material.parseItem());
        Objects.requireNonNull(getItemStack(), "Invalid item with material: " + material.name() + ". Make sure the material is placeable!");

        if (getItemStack().getItemMeta() == null)
            getItemStack().setItemMeta(Bukkit.getItemFactory().getItemMeta(material.parseMaterial()));

        //Load Display name
        section.ifValuePresent("display name", String.class, this::setDisplayName);

        //Load lore
        section.ifValuePresent("lore", List.class, this::setLore);

        // Load amount
        section.ifValuePresent("amount", Integer.class, this::setAmount);

        // Load stackable
        section.ifValuePresent("stackable", boolean.class, bool -> {
            if (bool)
                makeUnstackable();
        });

        // Load glow
        section.ifValuePresent("glow", boolean.class, bool -> {
            if (bool)
                makeGlow();
        });

        // Load Enchants
        section.ifValuePresent("enchants", List.class, list -> asListString(list, stringList -> {
            for (String enchant : stringList) {

                String[] split = enchant.split(":");
                if (split.length <= 1) continue;

                addEnchant(Enchantment.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]));
            }
        }));

        return (T) this;
    }

    public void save(ConfigSection section, OItem object) {
        // Set material
        section.set("material", object.getMaterial().name());

        // Set display name
        if (object.getDisplayName().length() > 0)
            section.set("display name", object.getDisplayName());

        // Set if glow
        if (object.isGlow())
            section.set("glow", true);

        if (object.getAmount() > 1)
            section.set("amount", object.getAmount());

        // Set lore
        if (!object.getLore().isEmpty())
            section.set("lore", object.getLore());

        // Set Enchants
        Set<OPair<Enchantment, Integer>> enchants = object.getEnchants();
        if (!enchants.isEmpty())
            section.set(
                    "enchants",
                    enchants.stream()
                            .map(enchant -> enchant.getFirst().getName() + ":" + enchant.getSecond())
                            .collect(Collectors.toList())
            );
    }

    private void asListString(List list, Consumer<List<String>> consumer) {
        consumer.accept(list);
    }

    public static <T extends ItemBuilder> ItemBuilder<T> fromConfiguration(ConfigSection section) {
        OMaterial material = OMaterial.matchMaterial(section.getAs("material", String.class)).get();
        Objects.requireNonNull(material, "Failed to find material by " + section.getAs("material", String.class));

        if (material.name().contains("HEAD"))
            return (ItemBuilder<T>) new OSkull().load(section);

        else if (material.name().contains("POTION"))
            return (ItemBuilder<T>) new OPotion(material).load(section);

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
            T clone = (T) getClass().getDeclaredConstructor(getClass()).newInstance(this);
            clone.onClone(this);
            return clone;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasEnchant(Enchantment enchantment) {
        if (getItemMeta() == null) return false;
        return getItemMeta().hasEnchant(enchantment);
    }

    public int getEnchantLevel(Enchantment enchantment) {
        if (getItemMeta() == null) return -1;
        return getItemMeta().getEnchantLevel(enchantment);
    }

    public ItemBuilder<T> setItemMeta(ItemMeta meta) {
        this.meta = meta;
        return this;
    }

    public ItemBuilder<T> removeLoreLineIf(Predicate<String> filter) {
        List<String> lore = getLore();
        lore.removeIf(filter);
        setLore(lore);
        return (T) this;
    }

    public void onClone(T from) {
    }

    public short getDurability() {
        return itemStack.getDurability();
    }

    protected NBTItem getNbt() {
        if (nbt == null)
            nbt = new NBTItem(itemStack);
        return nbt;
    }

    public T clearFlags() {
        if (meta == null) return (T) this;

        addItemFlag(ItemFlag.values());
        return (T) this;
    }
}
