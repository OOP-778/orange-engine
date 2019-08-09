package com.oop.orangeEngine.item.message;

import com.oop.orangeEngine.item.ItemStackUtil;
import com.oop.orangeEngine.main.Engine;
import com.oop.orangeEngine.message.additions.AAddition;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class HoverItemAddition extends AAddition {

    private ItemStack itemStack;

    @Override
    public void apply(TextComponent component) {

        if (itemStack != null) {
            try {

                BaseComponent[] components = new BaseComponent[]{new TextComponent(ItemStackUtil.itemStackToJson(itemStack))};
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, components));

            } catch (Exception ex) {
                Engine.getInstance().getLogger().error(ex);
            }
        }

    }

    public HoverItemAddition itemStack(ItemStack item) {
        this.itemStack = item;
        return this;
    }

    @Override
    public HoverItemAddition clone() {

        HoverItemAddition hia = ((HoverItemAddition) super.clone());
        hia.itemStack(itemStack.clone());

        return hia;

    }

    public TextComponent createMessage(String fullMessage, char hoverAt, ItemStack stack) {

        String splitAt = "[" + hoverAt + "]";
        String[] split = fullMessage.split(splitAt);

        TextComponent first = new TextComponent(TextComponent.fromLegacyText(color(split[0])));

        TextComponent hoverable = new TextComponent(splitAt);
        BaseComponent[] components = new BaseComponent[0];
        try {
            components = new BaseComponent[]{new TextComponent(ItemStackUtil.itemStackToJson(itemStack))};
        } catch (Exception e) {
            e.printStackTrace();
        }
        hoverable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, components));

        TextComponent last = new TextComponent(TextComponent.fromLegacyText(color(split[1])));

        first.addExtra(hoverable);
        first.addExtra(last);

        return first;

    }

    String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
