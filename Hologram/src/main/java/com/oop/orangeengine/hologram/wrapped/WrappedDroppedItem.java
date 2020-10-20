package com.oop.orangeengine.hologram.wrapped;

import com.oop.orangeengine.hologram.line.HologramItem;
import com.oop.orangeengine.hologram.util.UpdateableObject;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class WrappedDroppedItem extends WrappedEntity<HologramItem> {

    @Getter
    private UpdateableObject<ItemStack> itemStack = new UpdateableObject<>(null);

    public WrappedDroppedItem(HologramItem owner, Location location, @NonNull ItemStack itemStack) {
        super(owner, location);
        setEntity(ReflectionConstant.createItem(location, itemStack));
        this.itemStack.set(itemStack);

        // Defaults
        ReflectionConstant.invoke(ReflectionConstant.SET_CUSTOM_NAME_VISIBLE_METHOD, getEntity(), false);
    }
}
