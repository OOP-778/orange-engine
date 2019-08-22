package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.events.SyncEvents;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryController extends AEngineComponent {

    public InventoryController() {

        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {

        });

    }

    @Override
    public String getName() {
        return "Inventory Controller";
    }
}
