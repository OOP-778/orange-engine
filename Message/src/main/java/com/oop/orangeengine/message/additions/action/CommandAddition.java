package com.oop.orangeengine.message.additions.action;

import com.oop.orangeengine.message.additions.AAddition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

@AllArgsConstructor
public class CommandAddition extends AAddition {

    @Getter
    private String command;

    @Override
    public void apply(TextComponent component) {
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.startsWith("/") ? command : "/" + command));
    }

    @Override
    public void replace(String key, Object obj) {
        command = command.replace(key, obj.toString());
    }
}
