package com.oop.orangeengine.message;

import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Getter
public class OMessage implements Cloneable {

    private LinkedList<MessageLine> lineList = new LinkedList<>();
    private boolean center = false;

    public OMessage() {}

    public OMessage(String content) {
        lineList.add(new MessageLine(content));
    }

    public OMessage(List<String> list) {
        lineList.addAll(list.stream().map(MessageLine::new).collect(toList()));
    }

    public OMessage(MessageLine line) {
        lineList.add(line);
    }

    public OMessage(LineContent content) {
        lineList.add(new MessageLine(content));
    }

    public OMessage appendLine(MessageLine line) {
        this.lineList.add(line);
        return this;
    }

    public OMessage appendLine(String line) {
        this.lineList.add(new MessageLine().append(line));
        return this;
    }

    public OMessage setCenter(boolean globalCenter) {
        this.center = globalCenter;
        return this;
    }

    public void send(Player player) {

        if (center) lineList.forEach(line -> line.center(center));
        lineList.forEach(line -> line.send(player));

    }

    public void send(Player player, Map<String, String> placeholders) {

        if (center) lineList.forEach(line -> line.center(center));
        lineList.forEach(line -> line.send(player, placeholders));

    }

    public List<String> getRaw() {
        return lineList.stream()
                .map(MessageLine::getRaw)
                .collect(Collectors.toList());
    }

    @Override
    public OMessage clone() {
        OMessage message = new OMessage();
        message.lineList = new LinkedList<>();

        lineList.stream()
                .map(MessageLine::clone)
                .forEach(line -> message.lineList.add(line));

        message.center = center;
        return message;
    }

    public OMessage replace(String key, Object value) {
        lineList.forEach(line -> line.replace(key, value));
        return this;
    }

    public OMessage replace(Map<String, Object> placeholders) {
        lineList.forEach(line -> line.replace(placeholders));
        return this;
    }

    public OMessage replace(String key, LineContent content) {
        lineList.forEach(line -> line.replace(key, content));
        return this;
    }
}
