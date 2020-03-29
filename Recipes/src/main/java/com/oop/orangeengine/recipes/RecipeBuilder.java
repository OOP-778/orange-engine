package com.oop.orangeengine.recipes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Accessors(fluent = true, chain = true)
public class RecipeBuilder {

    @NonNull
    private char[][] pattern;

    @NonNull @Setter
    private ItemStack result;

    private Map<Character, ItemStack> charToItem = Maps.newHashMap();

    protected RecipeBuilder() {}

    public RecipeBuilder item(char character, @NonNull ItemStack itemStack) {
        Objects.requireNonNull(pattern, "Pattern first should be set!");

        charToItem.put(character, itemStack);
        return this;
    }

    public RecipeBuilder pattern(String ...pattern) {
        return pattern(Arrays.asList(pattern));
    }

    public RecipeBuilder pattern(List<String> pattern) {
        Preconditions.checkArgument(pattern.size() <= 3, "Recipe pattern cannot be longer than 3!");
        int x = 0, y = 0;
        this.pattern = new char[pattern.size()][pattern.size()];
        for (String pattern_line : pattern) {
            Preconditions.checkArgument(pattern_line.toCharArray().length <= 3, "Recipe pattern line cannot be longer than 3!");

            for (char c : pattern_line.toCharArray()) {
                if (c == ' ') continue;

                this.pattern[y][x] = c;
                x++;
            }

            x = 0;
            y++;
        }

        return this;
    }

    public Recipe build() {
        int len = pattern.length;
        ItemStack[][] matrix = new ItemStack[len][len];

        IntStream.of(0, len-1).forEach(y -> {
            IntStream.of(0, len-1).forEach(x -> {
                char charAt = pattern[y][x];
                ItemStack itemStack = charToItem.get(charAt);
                if (itemStack == null) return;

                matrix[y][x] = itemStack;
            });
        });

        if (len == 2)
            return new ORecipe_2x2(matrix, result);

        else
            return new ORecipe_3x3(matrix, result);
    }
}
