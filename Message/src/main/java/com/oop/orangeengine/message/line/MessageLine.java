package com.oop.orangeengine.message.line;

import com.google.common.primitives.Chars;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.message.Centered;
import com.oop.orangeengine.message.ColorFinder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class MessageLine {

    private LinkedList<LineContent> contentList = new LinkedList<>();
    private boolean centered = false;
    private boolean autoSpaces = false;

    public MessageLine insert(LineContent lineContent, LineContent at, InsertMethod method) {

        int indexOfAt = contentList.indexOf(at);
        if (indexOfAt == -1) throw new IllegalStateException("List doesn't contain location of message getValue!");

        switch (method) {
            case BEFORE:
                contentList.add(indexOfAt - 1, lineContent);
                return this;
            case AFTER:
                contentList.add(indexOfAt + 1, lineContent);

        }

        return this;

    }

    public MessageLine insert(LineContent lineContent, int at) {
        contentList.add(at, lineContent);
        return this;
    }

    public MessageLine append(LineContent lineContent) {
        contentList.add(lineContent);
        return this;
    }

    public MessageLine append(String content) {
        contentList.add(new LineContent(content));
        return this;
    }

    public void send(Player player) {
        send(player, new HashMap<>());
    }

    public void send(Player player, Map<String, String> placeholders) {

        String appendEnd = "", appendStart = "";

        if (centered) {

            //Okay so we need to gather setSpaces
            StringBuilder builder = new StringBuilder();
            contentList.forEach(c -> {

                String content = c.getText();
                for (String key : placeholders.keySet())
                    content = content.replace(key, placeholders.get(key));
                builder.append(content);

            });

            String centeredMessage = Centered.getCenteredMessage(builder.toString());
            LinkedList<Character> characterList = new LinkedList<>(Chars.asList(centeredMessage.toCharArray()));

            int spaceCount = 0;
            int startSpaces = 0, endSpaces = 0;

            for (Character charz : characterList) {

                if (!charz.toString().equalsIgnoreCase(" ")) {
                    startSpaces = spaceCount;
                    break;
                } else
                    spaceCount++;

            }

            Collections.reverse(characterList);
            spaceCount = 0;
            for (Character charz : characterList) {
                if (!charz.toString().equalsIgnoreCase(" ")) {
                    endSpaces = spaceCount;
                    break;
                } else spaceCount++;
            }

            //Append start setSpaces
            StringBuilder startBuilder = new StringBuilder();
            IntStream.range(1, startSpaces).forEach(s -> startBuilder.append(" "));

            //Append end setSpaces
            StringBuilder endBuilder = new StringBuilder();
            IntStream.range(1, endSpaces).forEach(s -> endBuilder.append(" "));

            //Finish off
            appendStart = startBuilder.toString();
            appendEnd = endBuilder.toString();
        }

        //Merge components
        TextComponent base = new TextComponent(appendStart);
        ColorFinder lastColorL = null;

        for (LineContent lineContent : contentList) {

            LineContent clonedLC = lineContent.clone();
            List<String> checkThrough = new ArrayList<>();
            StringBuilder buffer = new StringBuilder();

            if(lastColorL != null)
                buffer.append(lastColorL.color()).append(lastColorL.decoration());

            if(clonedLC.getText().contains(" "))
                checkThrough.addAll(Arrays.stream(clonedLC.getText().split(" ")).collect(toList()));
            else {

                lastColorL = ColorFinder.find(clonedLC.getText());
                buffer.append(clonedLC.getText());

            }

            for(String spacedString : checkThrough) {

                if(lastColorL == null) {

                    lastColorL = ColorFinder.find(spacedString);
                    buffer.append(spacedString).append(" ");

                } else {

                    ColorFinder colorFinder = ColorFinder.find(spacedString);
                    StringBuilder spacedBuffer = new StringBuilder(lastColorL.color() + lastColorL.decoration());

                    for(char character : spacedString.toCharArray()) {

                        if (!spacedBuffer.toString().contains(lastColorL.color() + lastColorL.decoration()))
                            spacedBuffer.append(lastColorL.color()).append(lastColorL.decoration()).append(character);

                        else
                            spacedBuffer.append(character);

                    }

                    if(colorFinder.color() != "")
                        lastColorL = colorFinder;

                    buffer.append(spacedBuffer).append(checkThrough.size() == 1 ? checkThrough.get(0).contains(" ") ? "" : "" : " ");

                }

            }

            //Set the Text
            clonedLC.text(buffer.toString());
            base.addExtra(clonedLC.create());

            if (autoSpaces)
                base.addExtra(new TextComponent(" "));

        }


        base.addExtra(new TextComponent(appendEnd));

        //Finish off by sending
        contentList.forEach(cl -> cl.triggerSend(player));
        player.spigot().sendMessage(base);

    }

    public MessageLine center(boolean center) {
        this.centered = center;
        return this;
    }

    public MessageLine autoSpaces(boolean autoSpaces) {
        this.autoSpaces = autoSpaces;
        return this;
    }

    public List<LineContent> contentList() {
        return contentList;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        MessageLine messageLine = ((MessageLine) super.clone());
        messageLine.contentList = new LinkedList<>();
        messageLine.contentList.addAll(contentList.stream().map(LineContent::clone).collect(toList()));

        return messageLine;

    }

    public enum InsertMethod {

        AFTER,
        BEFORE

    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        for(int index = length - 1; index > -1; --index) {
            char section = input.charAt(index);
            if ((section == '&' || section == '§') && index < length - 1) {

                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);
                if(color == null) continue;

                result.insert(0, color.toString());
                if (color.isColor() || color.equals(ChatColor.RESET))
                    break;
            }
        }

        return result.toString();
    }

}