package com.oop.orangeengine.message.impl;

import com.oop.orangeengine.main.util.OTitle;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.MessageType;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Accessors(chain = true, fluent = true)
@Setter
@Getter
public class OTitleMessage implements OMessage<OTitleMessage> {

    private String title;
    private String subTitle;
    private int fadeIn = 10;
    private int stay = 20;
    private int fadeOut = 10;

    @Override
    public String[] raw() {
        return Stream.of(title, subTitle).filter(Objects::nonNull).toArray(String[]::new);
    }

    @Override
    @SneakyThrows
    public OTitleMessage clone() {
        return (OTitleMessage) super.clone();
    }

    @Override
    public MessageType type() {
        return MessageType.TITLE;
    }

    @Override
    public void send(CommandSender... senders) {
        OTitle.sendTitle(fadeIn, stay, fadeOut, title, subTitle, Arrays.stream(senders)
                .filter(sender -> sender instanceof Player)
                .map(sender -> (Player) sender)
                .toArray(Player[]::new));
    }

    @Override
    public void send(Map<String, Object> placeholders, CommandSender... receivers) {
        OTitleMessage clone = clone();
        clone.replace(placeholders);
        clone.send(receivers);
    }

    @Override
    public OTitleMessage replace(Map<String, Object> placeholders) {
        String[] array = new String[]{title, subTitle};

        int i = -1;
        for (String s : array) {
            i++;
            if (s == null) {
                array[i] = null;
                continue;
            }

            for (String key : placeholders.keySet()) {
                s = s.replace(key, placeholders.get(key).toString());
            }
            array[i] = s;
        }

        title = array[0];
        subTitle = array[1];
        return returnThis();
    }

    @Override
    public <E> OTitleMessage replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        String[] array = new String[]{title, subTitle};

        int i = -1;
        for (String s : array) {
            i++;
            if (s == null) {
                array[i] = null;
                continue;
            }
            for (OPair<String, Function<E, String>> placeholder : placeholders) {
                s = s.replace(placeholder.getFirst(), placeholder.getSecond().apply(object));
            }
            array[i] = s;
        }

        title = array[0];
        subTitle = array[1];

        return returnThis();
    }

    @Override
    public OTitleMessage replace(Function<String, String> function) {
        this.title = function.apply(title);
        if (subTitle != null)
            this.subTitle = function.apply(subTitle);
        return this;
    }

    @Override
    public OTitleMessage returnThis() {
        return this;
    }
}
