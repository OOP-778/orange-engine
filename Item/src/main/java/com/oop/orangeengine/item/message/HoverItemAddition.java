package com.oop.orangeengine.item.message;

import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.message.additions.AAddition;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

}
