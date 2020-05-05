package com.oop.orangeengine.message.impl.chat.addition;

import com.oop.orangeengine.message.impl.chat.addition.impl.CommandAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.HoverItemAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.HoverTextAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.SuggestionAddition;

import java.util.Set;

public interface Additionable {
    HoverTextAddition hover();

    CommandAddition command();

    HoverItemAddition hoverItem();

    SuggestionAddition suggestion();

    Set<Addition> additionList();
}
