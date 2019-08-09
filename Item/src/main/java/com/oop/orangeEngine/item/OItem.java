package com.oop.orangeEngine.item;

import com.oop.orangeEngine.main.Helper;
import com.oop.orangeEngine.material.OMaterial;
import com.oop.orangeEngine.nbt.NBTItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class OItem {

    private ItemStack item;
    private List<String> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private List<ItemFlag> flagsToRemove = new ArrayList<>();
    private HashMap<String, Object> nbt_toAdd = new HashMap<>();
    private List<IEnchant> enchants_toAdd = new ArrayList<>();
    private List<Enchantment> enchants_toRemove = new ArrayList<>();
    private List<String> nbt_toRemove = new ArrayList<>();
    private boolean glow = false;

    public OItem(Material mat) {
        this(mat, 1);
    }

    public OItem(Material mat, int amount) {
        this(new ItemStack(mat, amount));
    }

    public OItem(ItemStack item) {
        this.item = item;

        if (item.getItemMeta() != null) {
            if (item.getItemMeta().hasLore()) setLore(item.getItemMeta().getLore(), true);
            if (item.getItemMeta().hasDisplayName()) setDisplayName(item.getItemMeta().getDisplayName());
        }


    }

    public OItem(OMaterial material) {
        this.item = material.parseItem();
    }

    public OItem addNbt(String key, Object ob2) {
        nbt_toAdd.put(key, ob2);
        return this;
    }

    public OItem appendLore(String string) {
        lore.add(Helper.color(string));
        return this;
    }

    public OItem removeLoreLine(int index) {
        lore.remove(index);
        return this;
    }

    public OItem setLoreLine(Integer index, String string) {
        lore.set(index, Helper.color(string));
        return this;
    }

    public ItemStack buildItem() {

        if (glow) item.addUnsafeEnchantment(Enchantment.LUCK, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setLore(lore);

            for (ItemFlag flag : flagsToRemove) {

                meta.removeItemFlags(flag);

            }
            for (ItemFlag flag : flags) {

                meta.addItemFlags(flag);

            }

            for (Enchantment enchant : enchants_toRemove) {

                meta.removeEnchant(enchant);

            }

            for (IEnchant enchantment : enchants_toAdd) {

                meta.addEnchant(enchantment.getEnchantment(), enchantment.getSize(), true);

            }

            if (glow) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }

        NBTItem nbt = new NBTItem(item);
        for (String key : nbt_toAdd.keySet()) {

            if (nbt_toAdd.get(key) instanceof String) nbt.setString(key, nbt_toAdd.get(key).toString());
            else nbt.setObject(key, nbt_toAdd.get(key));

        }

        for (String s : nbt_toRemove)
            nbt.removeKey(s);

        return nbt.getItem();
    }

    public OItem removeFromNbt(String key) {
        nbt_toRemove.add(key);
        return this;
    }

    public OItem clearLore() {
        lore.clear();
        return this;
    }

    public OItem setLore(List<String> l) {
        lore = l;
        return this;
    }

    public OItem replaceInLore(String key, String to) {
        List<String> copy = new ArrayList<>(lore);
        lore.clear();
        for (String s : copy) {
            s = s.replaceAll(key, to);
            lore.add(s);
        }
        return this;
    }

    public OItem setUnstackable() {
        nbt_toAdd.put("randomness_" + ThreadLocalRandom.current().nextInt(999999), "randomness_" + ThreadLocalRandom.current().nextInt(999999));
        return this;
    }

    public OItem addItemFlag(ItemFlag flag) {

        flags.add(flag);
        return this;

    }

    public OItem removeItemFlag(ItemFlag flag) {

        flagsToRemove.remove(flag);
        return this;

    }

    public OItem addEnchant(Enchantment enchant, int level) {

        enchants_toAdd.add(new IEnchant(enchant, level));
        return this;

    }

    public OItem removeEnchant(Enchantment enchant) {

        enchants_toRemove.add(enchant);
        return this;

    }

    public OItem setLore(List<String> lore, boolean coloured) {

        if (coloured) {

            List<String> s = new ArrayList<>();
            lore.forEach(s1 -> s.add(Helper.color(s1)));
            this.lore = s;

        }
        return this;

    }

    public OItem replaceInLore(Map<String, Object> replace) {

        List<String> newLore = new ArrayList<>();

        for (String l : this.lore) {

            for (String r : replace.keySet()) {
                l = l.replaceAll(r, replace.get(r).toString());
            }
            newLore.add(l);

        }
        this.lore = newLore;
        return this;
    }

    public OItem setDurability(byte data) {
        item.setDurability(data);
        return this;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public boolean isGlow() {
        return this.glow;
    }

    public OItem setDurability(int data) {
        return setDurability((byte) data);
    }

    public String getDisplayName() {
        return item.getItemMeta().getDisplayName();
    }

    public OItem setDisplayName(String displayName) {
        item.getItemMeta().setDisplayName(Helper.color(displayName));
        return this;
    }

    public void setMaterial(Material type) {
        this.item.setType(type);
    }

    public OItem replaceInDisplayName(String what, String to) {
        setDisplayName(getDisplayName().replace(what, to));
        return this;
    }

}
