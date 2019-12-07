package com.oop.orangeengine.recipes;

import com.google.common.collect.Sets;
import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.util.DefaultInitialization;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import java.util.List;
import java.util.Set;

public class RecipesController extends AEngineComponent {

    private Set<Recipe> registeredRecipes = Sets.newHashSet();

    @DefaultInitialization
    private RecipesController() {
        SyncEvents.listen(PrepareItemCraftEvent.class, EventPriority.LOWEST, event -> {

        });
    }

    @Override
    public String getName() {
        return "Recipes Controller";
    }

    public Recipe compileRecipe(List<String> rows) {
        return null;
    }

}
