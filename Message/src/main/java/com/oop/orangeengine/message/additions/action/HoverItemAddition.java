package com.oop.orangeengine.message.additions.action;

import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.message.additions.AAddition;
import com.oop.orangeengine.nbt.NBTItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

public class HoverItemAddition extends AAddition {

    private ItemStack itemStack;
    public HoverItemAddition(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void apply(TextComponent component) {
        if (itemStack != null) {
            try {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(NBTItem.convertItemtoNBT(itemStack).getCompound().toString()).create()));
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

}
