package com.oop.orangeengine.message.impl.chat.addition.impl;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Function;

import static com.oop.orangeengine.message.ChatUtil.makeSureNonNull;

@Setter
@Getter
@Accessors(chain = true, fluent = true)
public class HoverItemAddition implements Addition<HoverItemAddition> {
    private @NonNull ItemStack item;

    private LineContent content;
    public HoverItemAddition(LineContent content) {
        this.content = content;
    }

    @SneakyThrows
    @Override
    public HoverItemAddition clone() {
        return (HoverItemAddition) super.clone();
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(NBTItem.convertItemtoNBT(item).getCompound().toString()).create()));
    }

    @Override
    public LineContent parent() {
        return content;
    }

    @Override
    public void parent(LineContent parent) {
        this.content = parent;
    }

    @Override
    public HoverItemAddition replace(Map<String, Object> placeholders) {
        ItemMeta meta = getMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        String[] loreArray = lore.toArray(new String[0]);
        for (int i = 0; i < loreArray.length; i++) {
            int finalI = i;
            placeholders.forEach((key, value) -> loreArray[finalI] = loreArray[finalI].replace(key, value.toString()));
        }

        placeholders.forEach((key, value) -> meta.setDisplayName(meta.getDisplayName().replace(makeSureNonNull(key), makeSureNonNull(value))));
        meta.setLore(Arrays.asList(loreArray));

        return returnThis();
    }
    
    private ItemMeta getMeta() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        return meta;
    }

    @Override
    public <E> HoverItemAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        ItemMeta meta = getMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        String[] loreArray = lore.toArray(new String[0]);
        for (int i = 0; i < loreArray.length; i++) {
            int finalI = i;
            placeholders.forEach(pair -> loreArray[finalI] = loreArray[finalI].replace(makeSureNonNull(pair.getFirst()), makeSureNonNull(pair.getSecond().apply(object))));
        }

        placeholders.forEach(pair -> meta.setDisplayName(meta.getDisplayName().replace(makeSureNonNull(pair.getFirst()), makeSureNonNull(pair.getSecond().apply(object)))));
        meta.setLore(Arrays.asList(loreArray));

        return returnThis();
    }

    @Override
    public HoverItemAddition replace(@NonNull Function<String, String> function) {
        ItemMeta meta = getMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        String[] loreArray = lore.toArray(new String[0]);
        for (int i = 0; i < loreArray.length; i++)
            loreArray[0] = function.apply(loreArray[0]);

        meta.setDisplayName(function.apply(meta.getDisplayName()));
        meta.setLore(Arrays.asList(loreArray));

        return this;
    }

    @Override
    public HoverItemAddition returnThis() {
        return this;
    }
}
