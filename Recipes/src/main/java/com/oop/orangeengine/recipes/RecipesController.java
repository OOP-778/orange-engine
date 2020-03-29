package com.oop.orangeengine.recipes;

import com.google.common.collect.Sets;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RecipesController extends AEngineComponent {
    private static RecipesController INSTANCE;

    private Set<Recipe> registeredRecipes = Sets.newHashSet();
    private List<MatrixPacked> possibleSlotsOf2x2In3x3 = new ArrayList<>();

    static {
        new RecipesController();
    }

    private RecipesController() {
        INSTANCE = this;
        // Init possible slots for 2x2 in 3x3 env
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                MatrixPacked packed = new MatrixPacked(
                        r * 3 + c,
                        r * 3 + c + 1,
                        (r + 1) * 3 + c,
                        (r + 1) * 3 + c + 1
                );
                possibleSlotsOf2x2In3x3.add(packed);
            }
        }

        SyncEvents.listen(PrepareItemCraftEvent.class, EventPriority.LOWEST, event -> {
            int rows = event.getInventory().getSize() == 5 ? 2 : 3;

            Set<ORecipe_2x2> recipe_2x2s = Sets.newHashSet();
            Set<ORecipe_3x3> recipe_3x3s = Sets.newHashSet();
            registeredRecipes.forEach(recipe -> {
                if (recipe instanceof ORecipe_2x2)
                    recipe_2x2s.add((ORecipe_2x2) recipe);

                else if (recipe instanceof ORecipe_3x3)
                    recipe_3x3s.add((ORecipe_3x3) recipe);
            });

            AtomicReference<Recipe> recipe = new AtomicReference<>(null);

            // We're looking at 2x2 recipe else at 3x3
            if (rows == 2 && !recipe_2x2s.isEmpty()) {
                for (ORecipe_2x2 recipe_2x2 : recipe_2x2s) {
                    if (compareMatrixes(recipe_2x2, event.getInventory())) {
                        event.getInventory().setResult(recipe_2x2.getResult());
                        recipe.set(recipe_2x2);
                        break;
                    }
                }

            } else if (rows == 3) {
                for (ORecipe_3x3 recipe_3x3 : recipe_3x3s) {
                    if (compareMatrixes(recipe_3x3, event.getInventory())) {
                        event.getInventory().setResult(recipe_3x3.getResult());
                        recipe.set(recipe_3x3);
                        break;
                    }
                }

                // If recipe already found, return
                if (recipe.get() != null) return;

                ItemStack[] inv_matrix = event.getInventory().getMatrix();

                // Check for 2x2 in 3x3
                for (ORecipe_2x2 recipe_2x2 : recipe_2x2s) {
                    ItemStack[][] matrix = recipe_2x2.getMatrix();
                    ItemStack recipe_current = matrix[0][0];
                    ItemStack recipe_left = matrix[0][1];
                    ItemStack recipe_down = matrix[1][0];
                    ItemStack recipe_diag = matrix[1][1];

                    possibleSlotsOf2x2In3x3.forEach(packed -> {
                        if (recipe.get() != null) return;

                        ItemStack inv_current = inv_matrix[packed.getCurrent()];
                        ItemStack inv_left = inv_matrix[packed.getLeft()];
                        ItemStack inv_down = inv_matrix[packed.getDown()];
                        ItemStack inv_diag = inv_matrix[packed.getDiag()];

                        if (itemArrayEquals(recipe_current, recipe_left, recipe_down, recipe_diag, inv_current, inv_left, inv_down, inv_diag))
                            recipe.set(recipe_2x2);
                    });

                    if (recipe.get() != null) {
                        event.getInventory().setResult(recipe.get().getResult());
                        break;
                    }
                }
            }
        });
    }

    private boolean compareRecipeItem(ItemStack recipe_itemStack, ItemStack inventory_itemStack) {
        if (recipe_itemStack == null || recipe_itemStack.getType() == Material.AIR)
            return inventory_itemStack == null || inventory_itemStack.getType() == Material.AIR;

        if (inventory_itemStack == null || inventory_itemStack.getType() == Material.AIR) return false;
        if (recipe_itemStack.getAmount() > inventory_itemStack.getAmount()) return false;
        if (recipe_itemStack.getType() != inventory_itemStack.getType()) return false;

        NBTItem recipe_nbt = new NBTItem(recipe_itemStack);
        NBTItem inv_nbt = new NBTItem(inventory_itemStack);

        return recipe_nbt.asNBTString().contentEquals(inv_nbt.asNBTString());
    }

    @Override
    public String getName() {
        return "Recipes Controller";
    }

    public void registerRecipe(Recipe recipe) {
        this.registeredRecipes.add(recipe);
    }

    public Optional<Recipe> getRecipeFromResult(ItemStack itemStack) {
        return registeredRecipes
                .stream()
                .filter(recipe -> recipe.getResult().equals(itemStack))
                .findFirst();
    }

    private boolean compareMatrixes(Recipe recipe, CraftingInventory inventory) {
        ItemStack[][] matrix = recipe.getMatrix();
        int len = matrix.length;
        boolean break_bool = false;

        ItemStack[] inv_matrix = inventory.getMatrix();
        for (int y = 0; y < len; y++) {

            for (int x = 0; x < len; x++) {
                ItemStack recipe_itemStack = matrix[y][x];
                int slot = y == 0 ? x : y * x;
                ItemStack inventory_itemStack = inv_matrix[slot];

                if (!compareRecipeItem(recipe_itemStack, inventory_itemStack)) {
                    break_bool = true;
                    break;
                }
            }
            if (break_bool)
                break;
        }
        return !break_bool;
    }

    private boolean itemArrayEquals(ItemStack... itemStacks) {
        int mid = itemStacks.length / 2;

        ItemStack[] right = Arrays.copyOfRange(itemStacks, 0, mid);
        ItemStack[] left = Arrays.copyOfRange(itemStacks, mid, itemStacks.length);

        for (int i = 0; i < mid; i++) {
            if (!compareRecipeItem(right[i], left[i]))
                return false;
        }

        return true;
    }

    @AllArgsConstructor
    @Getter
    private class MatrixPacked {
        int current;
        int left;
        int down;
        int diag;

        @Override
        public String toString() {
            return "MatrixPacked{" +
                    "current=" + current +
                    ", left=" + left +
                    ", down=" + down +
                    ", diag=" + diag +
                    '}';
        }
    }

    public static RecipesController getInstance() {
        return INSTANCE;
    }
}
