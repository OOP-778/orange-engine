package com.oop.orangeengine.menu.events;

import com.oop.orangeengine.menu.button.ClickEnum;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ButtonFillEvent extends ButtonClickEvent {

    private final ItemStack fill;

    public ButtonFillEvent(ButtonClickEvent buttonClickEvent, ItemStack fill) {
        super(buttonClickEvent.getWrappedInventory(), buttonClickEvent.getMenu(), buttonClickEvent.getOriginalEvent(), buttonClickEvent.getPlayer(), buttonClickEvent.getClickedButton(), buttonClickEvent.getBeforeItem(), buttonClickEvent.getClickType());
        this.fill = fill;
    }
}
