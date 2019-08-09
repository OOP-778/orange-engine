package com.oop.orangeEngine.message;

import com.oop.orangeEngine.message.line.MessageLine;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Map;

@Getter
public class OMessage {

    private LinkedList<MessageLine> lineList = new LinkedList<>();
    private boolean center = false;

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

    @Override
    public OMessage clone() {
        try {
            return ((OMessage) super.clone());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
