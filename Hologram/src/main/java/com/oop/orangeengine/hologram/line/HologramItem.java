package com.oop.orangeengine.hologram.line;

import com.oop.orangeengine.hologram.HologramLine;
import com.oop.orangeengine.hologram.wrapped.WrappedDroppedItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class HologramItem extends HologramLine<HologramItem> {
    private OPair<Supplier<ItemStack>, Boolean> itemStackSupplier = new OPair<>(null, false);

    private WrappedDroppedItem droppedItem;

    public HologramItem(Supplier<ItemStack> itemStackSupplier) {
        this.itemStackSupplier.setFirst(itemStackSupplier);
    }

    public HologramItem(ItemStack itemStack) {
        this(() -> itemStack);
    }

    private ItemStack lastItemStack;
    private Set<Player> updateFor = new HashSet<>();

    @Override
    public void update() {
        if (location.isUpdated() && droppedItem != null) {
            setLocation(location.current().clone().add(0, 0.2, 0));
        }

        if (droppedItem == null) {
            setLocation(location.current().clone().add(0, 0.2, 0));
            droppedItem = new WrappedDroppedItem(this, getLocation().current(), itemStackSupplier.getFirst().get());
            getWrappedArmorStand().setCustomNameVisible(false);
        }

        for (Player player : updateFor) {
            droppedItem.spawn(player);
            getWrappedArmorStand().addPassenger(droppedItem.getEntity(), player);
            droppedItem.updateMeta(player);
        }
        updateFor.clear();

        ItemStack newItem = itemStackSupplier.getFirst().get();
        if (lastItemStack != null && lastItemStack.equals(newItem)) return;

        droppedItem.getItemStack().set(newItem);

        lastItemStack = newItem;
        droppedItem.update();
    }

    @Override
    public void remove() {
        super.remove();
        if (droppedItem != null)
            droppedItem.remove();
    }

    @Override
    public void handleAdd(Player player) {
        super.handleAdd(player);
        updateFor.add(player);
    }

}
