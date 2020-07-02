package com.oop.orangeengine.menu.newVersion.button.state;

import com.oop.orangeengine.menu.newVersion.button.MenuButton;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Accessors(chain = true, fluent = true)
public class ButtonStates implements Cloneable {

    private Map<String, ButtonState> states = new HashMap<>();

    @Getter
    private MenuButton button;

    public ButtonStates() {}

    public ButtonStates(MenuButton button) {
        this.button = button;
    }

    public ButtonStates clone() {
        ButtonStates states = new ButtonStates();
        this.states.forEach((id, state) -> states.states.put(id, state.clone().assignTo(states)));
        return states;
    }

    public Optional<ButtonState> get(String id) {
        return Optional.ofNullable(states.get(id));
    }

    public ButtonState getDefault() {
        return Objects.requireNonNull(states.get("default"), "Default state is not set!");
    }

    public ButtonStates addDefault(ItemStack itemStack) {
        add("default", itemStack);
        return this;
    }

    public ButtonStates add(String id, ItemStack itemStack) {
        states.remove(id);
        states.put(id, new ButtonState(this).itemStack(itemStack));
        return this;
    }
}
