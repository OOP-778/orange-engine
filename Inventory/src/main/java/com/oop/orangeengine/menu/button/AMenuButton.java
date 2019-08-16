package com.oop.orangeengine.menu.button;

import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.sound.WrappedSound;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class AMenuButton {

    private ItemStack currentItem;
    private int slot = -1;
    private WrappedInventory holder;
    private WrappedSound sound;

    private Map<ClickEnum, Consumer<ButtonClickEvent>> clickHandler = new HashMap<>();

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

    public void setCurrentItem(ItemStack itemStack) {
        currentItem = itemStack;
        if(holder != null)
            holder.updateButton(this);
    }

}
