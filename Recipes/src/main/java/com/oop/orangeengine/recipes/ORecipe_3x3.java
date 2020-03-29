package com.oop.orangeengine.recipes;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ORecipe_3x3 implements Recipe {
    private ItemStack[][] matrix;

    @Setter
    private ItemStack result;

    protected ORecipe_3x3(ItemStack[][] matrix, ItemStack result) {
        this.matrix = matrix;
        this.result = result;
    }
}
