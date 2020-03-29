package com.oop.orangeengine.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Testing {
    public static void main(String[] args) {
        Recipe
                .builder()
                .pattern(Arrays.asList("@ X", "@ X"))
                .item('X', new ItemStack(Material.DIAMOND))
                .item('@', new ItemStack(Material.ICE))
                .result(new ItemStack(Material.ACACIA_DOOR))
                .build();

        Recipe
                .builder()
                .pattern("XQX", "XQX", "XQX")
                .item('Q', new ItemStack(Material.DIAMOND_AXE))
                .result(new ItemStack(Material.DIAMOND_BLOCK))
                .build();
    }
}
