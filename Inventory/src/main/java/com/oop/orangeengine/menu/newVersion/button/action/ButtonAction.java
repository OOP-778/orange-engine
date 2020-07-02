package com.oop.orangeengine.menu.newVersion.button.action;

import com.oop.orangeengine.menu.newVersion.action.Action;
import lombok.Getter;

public class ButtonAction extends Action<ButtonAction> {

    @Getter
    private ButtonActions actions;

    public ButtonAction(ButtonActions actions) {
        this.actions = actions;
    }

    @Override
    protected ButtonAction returnThis() {
        return this;
    }
}
