package com.oop.orangeengine.message.impl.chat.addition.impl;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.oop.orangeengine.message.ChatUtil.makeSureNonNull;

@Accessors(chain = true, fluent = true)
public class ChatAddition implements Addition<ChatAddition> {

    @Setter
    @Getter
    private String message;

    private LineContent parent;
    public ChatAddition(LineContent parent) {
        this.parent = parent;
    }

    @Override
    public ChatAddition clone() {
        ChatAddition addition = new ChatAddition(parent);
        addition.message = message;
        return this;
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message));
    }

    @Override
    public ChatAddition replace(Map<String, Object> placeholders) {
        if (message == null) return this;
        for (String key : placeholders.keySet()) {
            message = message.replace(makeSureNonNull(key), makeSureNonNull(placeholders.get(key)));
        }
        return this;
    }

    @Override
    public <E> ChatAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) 
            message = message.replace(makeSureNonNull(placeholder.getFirst()), makeSureNonNull(placeholder.getSecond().apply(object)));
        
        return this;
    }

    @Override
    public ChatAddition replace(@NonNull Function<String, String> function) {
        message = function.apply(message);
        return this;
    }

    @Override
    public ChatAddition returnThis() {
        return this;
    }

    @Override
    public LineContent parent() {
        return parent;
    }

    @Override
    public void parent(LineContent parent) {
        this.parent = parent;
    }
}
