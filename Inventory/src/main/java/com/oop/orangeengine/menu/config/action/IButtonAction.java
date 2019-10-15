package com.oop.orangeengine.menu.config.action;

import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;

import java.util.function.Predicate;

public interface IButtonAction<T extends ButtonClickEvent> {

    void onAction(T event);

}
