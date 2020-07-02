package com.oop.orangeengine.menu.newVersion.button;

import com.oop.orangeengine.menu.newVersion.Menu;
import com.oop.orangeengine.menu.newVersion.button.action.ButtonActions;
import com.oop.orangeengine.menu.newVersion.button.animation.ButtonAnimations;
import com.oop.orangeengine.menu.newVersion.button.attribute.ButtonAttributes;
import com.oop.orangeengine.menu.newVersion.button.state.ButtonStates;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class MenuButton implements Cloneable {

    @Getter
    private Menu holder;

    @Getter
    private ButtonAttributes attributes = new ButtonAttributes(this);

    @Getter
    private ButtonStates states = new ButtonStates();

    @Getter
    private ButtonAnimations animations = new ButtonAnimations();

    @Getter
    private ButtonActions actions = new ButtonActions();

    @SneakyThrows
    public MenuButton clone() {
        MenuButton button = (MenuButton) super.clone();
        button.attributes = attributes.clone();
        button.states = states.clone();
        return button;
    }
}
