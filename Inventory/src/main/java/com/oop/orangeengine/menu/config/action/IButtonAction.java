package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;

public interface IButtonAction {

    void onAction(ButtonClickEvent button);

}
