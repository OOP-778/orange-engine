package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import com.oop.orangeengine.menu.events.ButtonEvent;

import java.util.function.Predicate;

public interface IButtonAction<T extends ButtonEvent> {

    void onAction(T event);

}
