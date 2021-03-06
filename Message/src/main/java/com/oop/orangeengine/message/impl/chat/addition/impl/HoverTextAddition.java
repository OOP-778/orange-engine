package com.oop.orangeengine.message.impl.chat.addition.impl;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.ChatUtil;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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

    @Getter
    private LineContent parent;

    public HoverTextAddition() {}
    public HoverTextAddition(LineContent parent) {
        this.parent = parent;
    }

    public HoverTextAddition add(@NonNull String... text) {
        hoverText.addAll(new ArrayList<>(Arrays.asList(text)));
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
        HoverTextAddition addition = new HoverTextAddition();
        addition.hoverText = new ArrayList<>(hoverText);
        return addition;
    }

    @Override
    public void apply(TextComponent textComponent) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Helper.color(String.join("\n", hoverText))).create()));
    }

    @Override
    public HoverTextAddition replace(Map<String, Object> placeholders) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++) {
            int finalI = i;
            placeholders.forEach((key, value) -> hoverArray[finalI] = hoverArray[finalI].replace(makeSureNonNull(key), makeSureNonNull(value)));
        }

        this.hoverText = new ArrayList<>(Arrays.asList(hoverArray));
        return returnThis();
    }

    @Override
    public <E> HoverTextAddition replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++) {
            int finalI = i;
            placeholders.forEach(pair -> hoverArray[finalI] = hoverArray[finalI].replace(makeSureNonNull(pair.getFirst()), makeSureNonNull(pair.getSecond().apply(object))));
        }

        this.hoverText = new ArrayList<>(Arrays.asList(hoverArray));
        return returnThis();
    }

    @Override
    public HoverTextAddition replace(@NonNull Function<String, String> function) {
        String[] hoverArray = hoverText.toArray(new String[0]);
        for (int i = 0; i < hoverArray.length; i++)
            hoverArray[i] = function.apply(hoverArray[i]);

        this.hoverText = new ArrayList<>(Arrays.asList(hoverArray));
        return returnThis();
    }

    @Override
    public HoverTextAddition returnThis() {
        return this;
    }

    @Override
    public String toString() {
        return "HoverTextAddition{" +
                "hoverText=" + ChatUtil.listToString(hoverText) +
                '}';
    }

    @Override
    public void parent(LineContent parent) {
        this.parent = parent;
    }
}
