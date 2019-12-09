package com.oop.orangeengine.menu;

import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.button.AMenuButton;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface ButtonHolder {

    List<AMenuButton> getButtons();

    default OptionalConsumer<AMenuButton> getButtonByFilter(Predicate<AMenuButton> filter) {
        return OptionalConsumer.of(
                getButtons().stream()
                        .filter(filter)
                        .findFirst()
        );
    }

    void removeButtonIfMatched(Predicate<AMenuButton> filter);

}
