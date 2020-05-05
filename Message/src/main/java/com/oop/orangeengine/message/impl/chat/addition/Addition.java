package com.oop.orangeengine.message.impl.chat.addition;

import com.oop.orangeengine.message.Replaceable;
import com.oop.orangeengine.message.impl.chat.LineContent;
import net.md_5.bungee.api.chat.TextComponent;

public interface Addition<T extends Addition> extends Cloneable, Replaceable<T>, Parentable<LineContent> {
    T clone();

    void apply(TextComponent textComponent);
}
