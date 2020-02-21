package com.oop.orangeengine.message.line;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.message.additions.AAddition;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Getter
public class LineContent implements Cloneable {

    private List<AAddition> additionList = new ArrayList<>();
    private String text;
    @Setter
    private List<String> hoverText = new ArrayList<>();

    public LineContent(String text) {
        this.text = text;
    }

    public LineContent addAddition(AAddition addition) {
        this.additionList.add(addition);
        return this;
    }

    public LineContent hoverText(String hoverText) {
        clearHover();
        appendHover(hoverText);
        return this;
    }

    public LineContent clearHover() {
        hoverText.clear();
        return this;
    }

    public LineContent appendHover(String hoverText) {
        this.hoverText.add(hoverText);
        return this;
    }

    public LineContent text(String text) {
        this.text = text;
        return this;
    }

    public TextComponent create(Map<String, String> placeholders) {
        String textCopy = text;
        List<String> hoverClone = new ArrayList<>(hoverText);

        for (String key : placeholders.keySet()) {
            String value = placeholders.get(key);

            textCopy = textCopy.replace(key, value);
            hoverClone = hoverText
                    .stream()
                    .map(text -> text.replace(key, value))
                    .collect(toList());
        }

        TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', textCopy));
        if (!hoverText.isEmpty()) {
            ComponentBuilder componentBuilder = new ComponentBuilder("");
            boolean[] first = new boolean[]{true};
            hoverClone.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).forEach(string -> {
                if (first[0]) {
                    first[0] = false;
                    componentBuilder.append(string);

                } else componentBuilder.append("\n").append(string);
            });
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, componentBuilder.create()));
        }

        additionList.forEach(addition -> addition.apply(textComponent));

        return textComponent;
    }

    public TextComponent create() {
        return create(new HashMap<>());
    }

    public void triggerSend(Player player) {
        additionList.forEach(ad -> ad.onSend(player));
    }

    @Override
    protected LineContent clone() {
        try {

            LineContent lineContent = (LineContent) super.clone();
            lineContent.additionList = new ArrayList<>();
            lineContent.additionList.addAll(additionList.stream().map(AAddition::clone).collect(toList()));

            return lineContent;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public LineContent replace(String key, Object value) {
        text = text.replace(key, value.toString());
        hoverText = hoverText
                .stream()
                .map(text -> text.replace(key, value.toString()))
                .collect(toList());
        return this;
    }

    public LineContent replace(Map<String, Object> placeholders) {
        placeholders.forEach(this::replace);
        return this;
    }
}
