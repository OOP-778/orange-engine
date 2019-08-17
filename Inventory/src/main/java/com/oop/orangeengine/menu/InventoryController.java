package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryController extends AEngineComponent {

    public InventoryController() {

        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {});

    }

    @Override
    public String getName() {
        return "Inventory Controller";
    }
}
