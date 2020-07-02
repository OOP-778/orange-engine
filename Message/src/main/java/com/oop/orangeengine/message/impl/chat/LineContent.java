package com.oop.orangeengine.message.impl.chat;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.Replaceable;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import com.oop.orangeengine.message.impl.chat.addition.Additionable;
import com.oop.orangeengine.message.impl.chat.addition.impl.CommandAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.HoverItemAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.HoverTextAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.SuggestionAddition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.oop.orangeengine.message.ChatUtil.makeSureNonNull;

public class LineContent implements Cloneable, Additionable, Replaceable<LineContent> {

    private Map<Class<? extends Addition>, Addition> additions = Maps.newConcurrentMap();

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private @NonNull String text;

    private LineContent() {}

    public LineContent(String text) {
        this.text = text;
    }

    public static LineContent of(String text) {
        return new LineContent(text);
    }

    @Override
    public LineContent clone() {
        LineContent clone = new LineContent();
        clone.text = text;
        additions.forEach((clazz, addition) -> clone.additions.put(clazz, addition.clone()));
        additions.values().forEach(addition -> addition.parent(this));
        return clone;
    }

    private <T extends Addition> T addAddition(Class<T> clazz, Supplier<T> supplier) {
        Addition addition = additions.get(clazz);
        if (addition == null) {
            addition = supplier.get();
            additions.put(clazz, addition);
        }
        return (T) addition;
    }

    @Override
    public HoverTextAddition hover() {
        return addAddition(HoverTextAddition.class, () -> new HoverTextAddition(this));
    }

    @Override
    public CommandAddition command() {
        return addAddition(CommandAddition.class, () -> new CommandAddition(this));
    }

    @Override
    public HoverItemAddition hoverItem() {
        return addAddition(HoverItemAddition.class, () -> new HoverItemAddition(this));
    }

    @Override
    public SuggestionAddition suggestion() {
        return addAddition(SuggestionAddition.class, () -> new SuggestionAddition(this));
    }

    @Override
    public Set<Addition> additionList() {
        return new HashSet<>(additions.values());
    }

    public TextComponent createComponent() {
        TextComponent textComponent = new TextComponent(text);
        additions.values().forEach(addition -> addition.apply(textComponent));
        return textComponent;
    }

    @Override
    public LineContent replace(Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            text = text.replace(makeSureNonNull(key), makeSureNonNull(placeholders.get(key)));
        }
        additions.values().forEach(addition -> addition.replace(placeholders));
        return returnThis();
    }

    @Override
    public <E> LineContent replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) {
            text = text.replace(makeSureNonNull(placeholder.getFirst()), makeSureNonNull(placeholder.getSecond().apply(object)));
        }
        additions.values().forEach(addition -> addition.replace(object, placeholders));
        return returnThis();
    }

    @Override
    public LineContent replace(@NonNull Function<String, String> function) {
        this.text = function.apply(text);
        additions.values().forEach(addition -> addition.replace(function));
        return this;
    }

    @Override
    public LineContent returnThis() {
        return this;
    }
}
