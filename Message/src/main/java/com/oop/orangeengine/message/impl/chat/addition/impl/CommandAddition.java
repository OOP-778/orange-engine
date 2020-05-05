package com.oop.orangeengine.message.impl.chat.addition.impl;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Accessors(chain = true, fluent = true)
public class CommandAddition implements Addition<CommandAddition> {
    @Getter @Setter
    private @NonNull String command;

    private @NonNull LineContent content;

    public CommandAddition set(String command) {
        this.command = command;
        return this;
    }

    public CommandAddition(LineContent lineContent) {
        this.content = lineContent;
    }

    @SneakyThrows
    @Override
    public CommandAddition clone() {
        return (CommandAddition) super.clone();
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command.startsWith("/") ? command : "/" + command));
    }

    @Override
    public LineContent parent() {
        return content;
    }

    @Override
    public void parent(LineContent parent) {
        this.content = parent;
    }

    @Override
    public CommandAddition replace(Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            command = command.replace(key, placeholders.get(key).toString());
        }
        return returnThis();
    }

    @Override
    public <E> CommandAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) {
            command = command.replace(placeholder.getFirst(), placeholder.getSecond().apply(object));
        }
        return returnThis();
    }
    @Override
    public CommandAddition returnThis() {
        return this;
    }
}
