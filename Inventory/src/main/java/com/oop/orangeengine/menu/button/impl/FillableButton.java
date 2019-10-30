package com.oop.orangeengine.menu.button.impl;

import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
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
        clickHandler(ButtonClickEvent.class, event -> {
            ItemStack filledWith = event.getClickedButton().currentItem();
            ButtonFillEvent buttonFillEvent = new ButtonFillEvent(event, filledWith);

            if (buttonFillEvent.isCancelled()) {
                event.pickupAtSlot();
                return;
            }

            if (filter != null && !filter.test(buttonFillEvent)) {
                event.pickupAtSlot();
                return;
            }

            ItemStack beforeItem = event.getBeforeItem();
            if (beforeItem.getType() == Material.AIR && filledWith.getType() == Material.AIR) return;

            if (beforeItem.getType() != Material.AIR && filledWith.getType() == Material.AIR) {
                ButtonEmptyEvent buttonEmptyEvent = new ButtonEmptyEvent(event, beforeItem.clone());

                // Per button listener
                event.getClickedButton().clickListeners().stream().filter(listener -> listener.accepts(buttonEmptyEvent)).forEach(listener -> listener.consumer().accept(buttonEmptyEvent));

                // Global button events
                event.getMenu().actionSet().stream()
                        .filter(props -> props.accepts(buttonEmptyEvent))
                        .forEach(props -> props.buttonAction().onAction(buttonEmptyEvent));

                if (buttonEmptyEvent.isCancelled()) {
                    event.pickupAtSlot();
                    return;
                }
                if (buttonEmptyHandler != null) {
                    buttonEmptyHandler.accept(buttonEmptyEvent);
                }

                return;
            }

            if (buttonFillHandler != null) {
                event.getClickedButton().clickListeners().stream().filter(listener -> listener.accepts(buttonFillEvent)).forEach(listener -> {
                    if (!buttonFillEvent.isCancelled())
                        listener.consumer().accept(buttonFillEvent);
                });
                event.getMenu().actionSet().stream()
                        .filter(props -> props.accepts(buttonFillEvent))
                        .forEach(props -> {
                            if (!buttonFillEvent.isCancelled())
                                props.buttonAction().onAction(buttonFillEvent);
                        });

                // Cancel operation if the fill event is cancelled
                if (buttonFillEvent.isCancelled()) {
                    event.pickupAtSlot();
                    return;
                }
                buttonFillHandler.accept(buttonFillEvent);
            }
        });
    }

    public void clean() {
        currentItem(OMaterial.AIR.parseItem());
    }

    public boolean isFilled() {
        return currentItem().getType() != Material.AIR;
    }
}
