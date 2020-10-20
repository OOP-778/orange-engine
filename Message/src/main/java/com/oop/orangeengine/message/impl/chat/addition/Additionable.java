package com.oop.orangeengine.message.impl.chat.addition;

import com.oop.orangeengine.message.impl.chat.addition.impl.*;

import java.util.Set;

public interface Additionable {
    HoverTextAddition hover();

    CommandAddition command();

    HoverItemAddition hoverItem();

    SuggestionAddition suggestion();

    ChatAddition chat();

    Set<Addition> additionList();
}
