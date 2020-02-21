package com.oop.orangeengine.item.message;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.additions.action.HoverItemAddition;
import com.oop.orangeengine.message.line.LineContent;
import org.bukkit.inventory.ItemStack;

public class ItemLineContent extends LineContent {

    public ItemLineContent(ItemStack itemStack) {
        super((itemStack.getAmount() == 1 ? "" : "x" + itemStack.getAmount() + " ") + (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : Helper.beautify(OMaterial.matchMaterial(itemStack))));
        addAddition(new HoverItemAddition(itemStack));
    }

    public ItemLineContent(String text) {
        super(text);
        throw new IllegalStateException("Not supported!");
    }

}
