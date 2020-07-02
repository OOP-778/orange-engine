package com.oop.orangeengine.menu.newVersion.button.state;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

@Accessors(chain = true, fluent = true)
public class ButtonState implements Cloneable {

    @Getter @Setter
    private ItemStack itemStack;

    @Getter
    private ButtonStates states;

    public ButtonState(ButtonStates states) {
        this.states = states;
    }

    public ButtonState clone() {
        ButtonState state = new ButtonState(states);
        return state;
    }

    public ButtonState assignTo(ButtonStates states) {
        this.states = states;
        return this;
    }

}
