package com.oop.orangeengine.menu.newVersion.button.action;

import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Accessors(chain = true, fluent = true)
public class ButtonActions {

    private Set<ButtonAction> actions = new HashSet<>();

    public ButtonActions clear() {
        actions.clear();
        return this;
    }

    public ButtonActions addTarget(ButtonAction action) {
        actions.add(action);
        return this;
    }
}
