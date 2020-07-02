package com.oop.orangeengine.message.impl.chat.addition.impl;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.function.Function;

import static com.oop.orangeengine.message.ChatUtil.makeSureNonNull;

@Accessors(fluent = true, chain = true)
public class HoverTextAddition implements Addition<HoverTextAddition> {

    @Getter
    private List<String> hoverText = new ArrayList<>();

    private LineContent content;
    public HoverTextAddition(LineContent content) {
        this.content = content;
    }

    public HoverTextAddition add(@NonNull String... text) {
        hoverText.addAll(Arrays.asList(text));
        return this;
    }

    public HoverTextAddition set(@NonNull List<String> hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public HoverTextAddition set(@NonNull String... text) {
        return set(Arrays.asList(text));
    }

    public HoverTextAddition clear() {
        hoverText.clear();
        return this;
    }

    @Override
    @SneakyThrows
    public HoverTextAddition clone() {
        return (HoverTextAddition) super.clone();
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Helper.color(String.join("\n", hoverText))).create()));
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
    public HoverTextAddition replace(Map<String, Object> placeholders) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++) {
            int finalI = i;
            placeholders.forEach((key, value) -> hoverArray[finalI] = hoverArray[finalI].replace(makeSureNonNull(key), makeSureNonNull(value)));
        }

        this.hoverText = Arrays.asList(hoverArray);
        return returnThis();
    }

    @Override
    public <E> HoverTextAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++) {
            int finalI = i;
            placeholders.forEach(pair -> hoverArray[finalI] = hoverArray[finalI].replace(makeSureNonNull(pair.getFirst()), makeSureNonNull(pair.getSecond().apply(object))));
        }

        this.hoverText = Arrays.asList(hoverArray);
        return returnThis();
    }

    @Override
    public HoverTextAddition replace(@NonNull Function<String, String> function) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++)
            hoverArray[i] = function.apply(hoverArray[i]);

        this.hoverText = Arrays.asList(hoverArray);
        return returnThis();
    }

    @Override
    public HoverTextAddition returnThis() {
        return this;
    }
}
