package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonEmptyEvent;
import com.oop.orangeengine.menu.events.ButtonFillEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

@EqualsAndHashCode
@Accessors(chain = true)
public class FillableButton extends AMenuButton {

    @Getter
    @Setter
    private Consumer<ButtonFillEvent> buttonFillHandler;

    @Getter
    @Setter
    private Consumer<ButtonEmptyEvent> buttonEmptyHandler;

    @Getter
    @Setter
    private Predicate<ButtonFillEvent> filter;

    public FillableButton() {
        super(OMaterial.AIR.parseItem(), -1);

        pickable(true);
        clickHandler(event -> {
            ItemStack filledWith = event.getClickedButton().currentItem();
            ButtonFillEvent buttonFillEvent = new ButtonFillEvent(event, filledWith);

            if (filter != null && !filter.test(buttonFillEvent)) {
                event.switchCursorWithSlot();
                return;
            }

            ItemStack beforeItem = event.getBeforeItem();
            if (beforeItem.getType() == Material.AIR && filledWith.getType() == Material.AIR) return;

            if (beforeItem.getType() != Material.AIR && filledWith.getType() == Material.AIR) {
                if (buttonEmptyHandler != null)
                    buttonEmptyHandler.accept(new ButtonEmptyEvent(event, beforeItem.clone()));

                return;
            }

            if (buttonFillHandler != null)
                buttonFillHandler.accept(buttonFillEvent);
        });
    }

    public void clean() {
        currentItem(OMaterial.AIR.parseItem());
    }

}
