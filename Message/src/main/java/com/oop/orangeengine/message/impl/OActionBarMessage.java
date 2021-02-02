package com.oop.orangeengine.message.impl;

import com.oop.orangeengine.main.util.OActionBar;
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
import java.util.Set;
import java.util.function.Function;

public class OActionBarMessage implements OMessage<OActionBarMessage> {
    @Getter @Setter
    @Accessors(chain = true, fluent = true)
    private @NonNull String text;

    @Override
    public String[] raw() {
        return new String[]{text};
    }

    @SneakyThrows
    @Override
    public OActionBarMessage clone() {
        return (OActionBarMessage) super.clone();
    }

    @Override
    public MessageType type() {
        return MessageType.ACTION_BAR;
    }

    @Override
    public void send(CommandSender ...senders) {
        OActionBar.sendActionBar(text, Arrays.stream(senders)
                .filter(sender -> sender instanceof Player)
                .map(sender -> (Player) sender)
                .toArray(Player[]::new));
    }

    @Override
    public OActionBarMessage replace(Map<String, Object> placeholders) {
        for (String key : placeholders.keySet()) {
            text = text.replace(key, placeholders.get(key).toString());
        }
        return returnThis();
    }

    @Override
    public <E> OActionBarMessage replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        for (OPair<String, Function<E, String>> placeholder : placeholders) {
            text = text.replace(placeholder.getFirst(), placeholder.getSecond().apply(object));
        }
        return returnThis();
    }

    @Override
    public OActionBarMessage replace(@NonNull Function<String, String> function) {
        this.text = function.apply(text);
        return this;
    }

    @Override
    public OActionBarMessage returnThis() {
        return this;
    }

    @Override
    public void send(Map<String, Object> placeholders, CommandSender... receivers) {
        OActionBarMessage clone = clone();
        clone.replace(placeholders);
        clone.send(receivers);
    }

    @Override
    public String toString() {
        return "OActionBarMessage{" +
                "text='" + text + '\'' +
                '}';
    }
}
