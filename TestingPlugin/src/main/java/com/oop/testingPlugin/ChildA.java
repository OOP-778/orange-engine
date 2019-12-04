package com.oop.testingPlugin;

import com.oop.orangeengine.item.custom.OItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChildA implements Serializable {

    private String wagawgaw = "awgawgawgfawfg";
    private List<ItemStack> itemStackList = new ArrayList<>();

    public ChildA() {
        for (int a = 0; a < 100; a++)
            itemStackList.add(
                    new OItem(Material.CARROT_ITEM)
                            .setDisplayName("awgawgawgawga")
                            .appendLore("AWFawfaw" + a)
                            .getItemStack()
            );
    }

}
