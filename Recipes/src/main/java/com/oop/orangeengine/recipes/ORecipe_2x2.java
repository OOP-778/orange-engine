package com.oop.orangeengine.recipes;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ORecipe_2x2 implements Recipe {
    private ItemStack[][] matrix;

    @Setter
    private ItemStack result;

    protected ORecipe_2x2(ItemStack[][] matrix, ItemStack result) {
        this.matrix = matrix;
        this.result = result;
    }
}
