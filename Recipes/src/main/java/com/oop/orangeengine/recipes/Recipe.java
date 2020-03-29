package com.oop.orangeengine.recipes;

import org.bukkit.inventory.ItemStack;

public interface Recipe {
    ItemStack[][] getMatrix();

    ItemStack getResult();

    static RecipeBuilder builder() {
        return new RecipeBuilder();
    }

}
