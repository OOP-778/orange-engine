package com.oop.orangeEngine.message.additions.action;

import com.oop.orangeEngine.message.additions.AAddition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

@RequiredArgsConstructor
public class CommandAddition extends AAddition {

    @Getter
    private final String command;

    @Override
    public void apply(TextComponent component) {
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

}
