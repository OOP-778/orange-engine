package com.oop.orangeengine.message.line;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.message.additions.AAddition;
import lombok.Getter;
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
    private String hoverText;

    public LineContent(String text) {
        this.text = text;
    }

    public LineContent addAddition(AAddition addition) {
        this.additionList.add(addition);
        return this;
    }

    public LineContent hoverText(String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public LineContent text(String text) {
        this.text = text;
        return this;
    }

    public TextComponent create(Map<String, String> placeholders) {

        String textCopy = text;
        String hoverCopy = hoverText != null ? hoverText : "";

        for (String key : placeholders.keySet()) {

            textCopy = textCopy.replace(key, placeholders.get(key));
            hoverCopy = hoverCopy.replace(key, placeholders.get(key));

        }

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', textCopy)));
        if (hoverText != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Helper.color(hoverCopy)).create()));

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
}
