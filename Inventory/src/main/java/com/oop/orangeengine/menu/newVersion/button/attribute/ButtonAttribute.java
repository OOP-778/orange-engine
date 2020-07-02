package com.oop.orangeengine.menu.newVersion.button.attribute;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ButtonAttribute {

    // When button is doing nothing else, just sits there and uses the slot...
    FILLER(false),

    // When button is there just for template, mostly used in paged menus!
    TEMPLATE(false);

    private Object defaultValue;
}
