package com.oop.orangeengine.menu.button;

import com.oop.orangeengine.main.storage.Storegable;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.sound.WrappedSound;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public abstract class AMenuButton extends Storegable implements Cloneable {

    @Getter
    private final Map<ClickEnum, Consumer<ButtonClickEvent>> clickHandler = new HashMap<>();

    @Getter
    private ItemStack currentItem;

    @Getter
    private int slot;

    @Setter
    @Getter
    private WrappedInventory holder;

    @Setter
    @Getter
    private WrappedSound sound;

    @Setter
    @Getter
    private boolean pickable = true;

    @Getter
    @Setter
    private boolean paged = false;

    @Getter
    @Setter
    private boolean forceCancelEvent = false;

    public AMenuButton(ItemStack currentItem, int slot) {
        this.currentItem = currentItem;
        this.slot = slot;
    }

    public AMenuButton clickHandler(ClickEnum clickEnum, Consumer<ButtonClickEvent> event) {
        clickHandler.remove(clickEnum);
        clickHandler.put(clickEnum, event);

        return this;
    }

    public AMenuButton clickHandler(Consumer<ButtonClickEvent> event) {
        clickHandler.remove(ClickEnum.GLOBAL);
        clickHandler.put(ClickEnum.GLOBAL, event);

        return this;
    }

    public AMenuButton currentItem(ItemStack itemStack) {
        currentItem = itemStack;
        if (holder != null)
            holder.updateButton(this);

        return this;
    }

    public AMenuButton slot(int slot) {
        if (holder != null)
            holder.setButton(this.slot, null);

        this.slot = slot;
        return this;
    }

    @Override
    public AMenuButton clone() {
        AMenuButton aMenuButton = null;
        try {
            aMenuButton = (AMenuButton) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert aMenuButton != null;

        aMenuButton.holder = null;
        aMenuButton.currentItem = currentItem.clone();
        return aMenuButton;
    }
}
