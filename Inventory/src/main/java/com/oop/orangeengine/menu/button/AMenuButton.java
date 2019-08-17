package com.oop.orangeengine.menu.button;

import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.sound.WrappedSound;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public abstract class AMenuButton extends Storegable {

    @Getter
    private ItemStack currentItem;
    @Getter
    private int slot;
    @Setter @Getter
    private WrappedInventory holder;
    @Setter @Getter
    private WrappedSound sound;

    private final Map<ClickEnum, Consumer<ButtonClickEvent>> clickHandler = new HashMap<>();

    public AMenuButton(ItemStack currentItem, int slot) {
        this.currentItem = currentItem;
        this.slot = slot;
    }

    public AMenuButton addClickHandler(ClickEnum clickEnum, Consumer<ButtonClickEvent> event) {
        clickHandler.remove(clickEnum);
        clickHandler.put(clickEnum, event);

        return this;
    }

    public AMenuButton addClickHandler(Consumer<ButtonClickEvent> event) {
        clickHandler.remove(ClickEnum.GLOBAL);
        clickHandler.put(ClickEnum.GLOBAL, event);

        return this;
    }

    public AMenuButton setCurrentItem(ItemStack itemStack) {
        currentItem = itemStack;
        if (holder != null)
            holder.updateButton(this);

        return this;
    }

}
