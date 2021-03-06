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

import static com.oop.orangeengine.message.ChatUtil.makeSureNonNull;


@Accessors(chain = true, fluent = true)
public class SuggestionAddition implements Addition<SuggestionAddition> {
    @Getter
    @Setter
    private @NonNull String suggestion;

    @Getter
    private LineContent parent;

    public SuggestionAddition() {}
    public SuggestionAddition(LineContent parent) {
        this.parent = parent;
    }

    @Override
    @SneakyThrows
    public SuggestionAddition clone() {
        SuggestionAddition addition = new SuggestionAddition();
        addition.suggestion = suggestion;
        return addition;
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
    }

    @Override
    public SuggestionAddition replace(Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            suggestion = suggestion.replace(makeSureNonNull(key), makeSureNonNull(placeholders.get(key)));
        }
        return returnThis();
    }

    @Override
    public <E> SuggestionAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) {
            suggestion = suggestion.replace(makeSureNonNull(placeholder.getFirst()), makeSureNonNull(placeholder.getSecond().apply(object)));
        }
        return returnThis();
    }

    @Override
    public SuggestionAddition replace(@NonNull Function<String, String> function) {
        this.suggestion = function.apply(suggestion);
        return this;
    }

    @Override
    public SuggestionAddition returnThis() {
        return this;
    }

    @Override
    public String toString() {
        return "SuggestionAddition{" +
                "suggestion='" + suggestion + '\'' +
                '}';
    }

    @Override
    public void parent(LineContent parent) {
        this.parent = parent;
    }
}
