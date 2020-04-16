package com.oop.orangeengine.message.line;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Chars;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.Centered;
import com.oop.orangeengine.message.Contentable;
import com.oop.orangeengine.message.WordsQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class MessageLine implements Cloneable, Contentable {

    private LinkedList<LineContent> contentList = new LinkedList<>();
    private boolean center = false;
    private boolean autoSpaces = false;
    private boolean cache = false;

    private TextComponent cached;

    public MessageLine() {
    }

    public MessageLine(String content) {
        contentList.add(new LineContent(content));
    }

    public MessageLine(LineContent content) {
        contentList.add(content);
    }


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

    public MessageLine replace(LineContent replace, LineContent to) {
        int i = contentList.indexOf(replace);
        Preconditions.checkArgument(i != -1);

        contentList.set(i, to);
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

    public MessageLine replace(String key, Object value) {
        contentList.forEach(content -> content.replace(key, value));
        return this;
    }

    public void replace(Map<String, Object> placeholders) {
        contentList.forEach(content -> content.replace(placeholders));
    }

    public void send(Player player) {
        send(player, new HashMap<>());
    }

    public void send(Player player, Map<String, String> placeholders) {
        if (placeholders.isEmpty() && cached != null) {
            contentList.forEach(cl -> cl.triggerSend(player));
            player.spigot().sendMessage(cached);
            return;
        }

        String appendEnd = "", appendStart = "";
        if (center) {

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

                } else
                    spaceCount++;
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

        // Merge components
        TextComponent base = new TextComponent(appendStart);
        WordsQueue lastQueue = null;

        for (LineContent lineContent : contentList) {
            LineContent clonedLC = lineContent.clone();

            String[] forLambda = new String[]{lineContent.getText()};
            placeholders.forEach((key, plac) -> forLambda[0] = forLambda[0].replace(key, plac));

            StringBuilder builder = new StringBuilder();
            lastQueue = WordsQueue.of(forLambda[0], lastQueue == null ? new WordsQueue.WordDecoration(null, null) : lastQueue.getEndDecoration());
            lastQueue.getWords().forEach(word -> builder.append(word.getString()));

            // Set the Text
            clonedLC.text(builder.toString());

            List<String> hoverText = new ArrayList<>();
            for (String text : clonedLC.getHoverText()) {
                forLambda[0] = text;
                placeholders.forEach((key, plac) -> forLambda[0] = forLambda[0].replace(key, plac));
                hoverText.add(forLambda[0]);
            }

            // Set hover text
            clonedLC.setHoverText(hoverText);

            base.addExtra(clonedLC.create());

            if (autoSpaces)
                base.addExtra(new TextComponent(" "));
        }

        base.addExtra(new TextComponent(appendEnd));
        this.cached = base;

        // Finish off by sending
        contentList.forEach(cl -> cl.triggerSend(player));
        player.spigot().sendMessage(base);

        if (cache)
            cached = base;
    }

    @Override
    public MessageLine clone() {
        MessageLine messageLine = null;
        try {
            messageLine = ((MessageLine) super.clone());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (messageLine != null) {
            messageLine.contentList = new LinkedList<>();
            messageLine.contentList.addAll(contentList.stream().map(LineContent::clone).collect(toList()));
        }

        return messageLine;
    }

    @Override
    public <T> void replace(T object, Set<OPair<String, Function<T, String>>> placeholders) {
        contentList.forEach(content -> content.replace(object, placeholders));
    }

    public enum InsertMethod {

        AFTER,
        BEFORE

    }

    public String getRaw() {
        String[] rawText = new String[]{""};
        String space = autoSpaces() ? " " : "";

        contentList.forEach(lineContent -> rawText[0] = space + lineContent.getText());

        return rawText[0];
    }

    public MessageLine replace(String key, LineContent content) {
        /*
        Let's say key is %test% whenever found in content it should split the content up in two parts
        And insert the content at the place where it was split
        */
        for (LineContent lineContent : new LinkedList<>(this.contentList)) {
            if (!lineContent.getText().contains(key)) continue;

            String[] split = lineContent.getText().split(key);
            if (split.length == 2) {
                LineContent firstPart = new LineContent(split[0]);
                LineContent secondPart = content.clone();
                LineContent thirdPart = new LineContent(split[1]);

                replace(lineContent, firstPart);
                insert(secondPart, firstPart, InsertMethod.AFTER);
                insert(thirdPart, secondPart, InsertMethod.AFTER);

            } else if (split.length == 1) {
                LineContent firstPart = new LineContent(split[0]);
                replace(lineContent, firstPart);
                insert(content, firstPart, InsertMethod.AFTER);
            }
        }
        return this;
    }

    public void remove(LineContent lineContent) {
        contentList.remove(lineContent);
    }

    public void removeContentIf(Predicate<LineContent> filter) {
        contentList.removeIf(filter);
    }
}
