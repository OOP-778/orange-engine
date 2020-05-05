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

@Setter
@Getter
@Accessors(chain = true, fluent = true)
public class SuggestionAddition implements Addition<SuggestionAddition> {
    private @NonNull String suggestion;

    private LineContent content;
    public SuggestionAddition(LineContent content) {
        this.content = content;
    }

    @Override
    @SneakyThrows
    public SuggestionAddition clone() {
        return (SuggestionAddition) super.clone();
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
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
    public SuggestionAddition replace(Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            suggestion = suggestion.replace(key, placeholders.get(key).toString());
        }
        return returnThis();
    }

    @Override
    public <E> SuggestionAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) {
            suggestion = suggestion.replace(placeholder.getFirst(), placeholder.getSecond().apply(object));
        }
        return returnThis();
    }
    @Override
    public SuggestionAddition returnThis() {
        return this;
    }
}
