package com.oop.orangeengine.message.impl.chat;

import com.google.common.base.Preconditions;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.Replaceable;
import com.oop.orangeengine.message.Sendable;
import com.oop.orangeengine.message.impl.OChatMessage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Accessors(chain = true, fluent = true)
public class ChatLine implements Replaceable<ChatLine>, Cloneable, Sendable {

    @Getter
    private LinkedList<LineContent> contentList = new LinkedList<>();

    @Getter
    @Setter
    private boolean centered = false;

    public ChatLine(String content) {
        this(LineContent.of(content));
    }

    public ChatLine(LineContent... content) {
        contentList.addAll(Arrays.asList(content));
    }

    public ChatLine insert(LineContent lineContent, LineContent at, InsertMethod method) {
        int indexOfAt = contentList.indexOf(at);
        if (indexOfAt == -1) throw new IllegalStateException("List doesn't contain location of message getObject!");

        switch (method) {
            case BEFORE:
                contentList.add(indexOfAt - 1, lineContent);
                return this;

            case AFTER:
                contentList.add(indexOfAt + 1, lineContent);
        }

        return this;
    }

    public ChatLine insert(LineContent what, int at) {
        contentList.add(at, what);
        return this;
    }

    public ChatLine replace(LineContent replace, LineContent to) {
        int i = contentList.indexOf(replace);
        Preconditions.checkArgument(i != -1);

        contentList.set(i, to);
        return this;
    }

    public ChatLine append(LineContent content) {
        contentList.add(content);
        return this;
    }

    public ChatLine append(String content) {
        return append(LineContent.of(content));
    }

    public ChatLine replace(String key, LineContent content) {
        for (LineContent lineContent : new LinkedList<>(this.contentList)) {
            if (!lineContent.text().contains(key)) continue;

            String[] split = lineContent.text().split(key);
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

    @Override
    public ChatLine replace(Map<String, Object> placeholders) {
        contentList.forEach(content -> content.replace(placeholders));
        return this;
    }

    @Override
    public <E> ChatLine replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        contentList.forEach(content -> content.replace(object, placeholders));
        return this;
    }

    @Override
    public ChatLine returnThis() {
        return this;
    }

    @Override
    public void send(CommandSender ...receivers) {
        if (Arrays.stream(receivers).noneMatch(receiver -> receiver instanceof Player)) {
            String raw = Helper.color(raw());
            for (CommandSender receiver : receivers)
                receiver.sendMessage(raw);

        } else {
            StringBuilder appendStart = new StringBuilder(), appendEnd = new StringBuilder();
            if (centered) {
                String content = contentList.stream().map(LineContent::text).collect(Collectors.joining());
                IntStream.range(1, findSpaces(content, false)).forEach(i -> appendStart.append(" "));
                IntStream.range(1, findSpaces(content, true)).forEach(i -> appendEnd.append(" "));
            }

            TextComponent base = new TextComponent(appendStart.toString());
            WordsQueue lastQueue = null;

            for (LineContent lineContent : contentList) {
                LineContent clone = lineContent.clone();
                WordsQueue queue = WordsQueue.of(clone.text(), lastQueue == null ? new WordsQueue.WordDecoration(null, null) : lastQueue.getEndDecoration());
                clone.text(queue.getWords().stream().map(WordsQueue.Word::getString).collect(Collectors.joining("")));

                base.addExtra(clone.createComponent());
                lastQueue = queue;
            }

            base.addExtra(appendEnd.toString());

            for (CommandSender receiver : receivers) {
                if (receiver instanceof Player)
                    ((Player) receiver).spigot().sendMessage(base);

                else
                    receiver.sendMessage(Helper.color(base.toLegacyText()));
            }
        }
    }

    @Override
    public void send(Map<String, Object> placeholders, CommandSender... receivers) {
        ChatLine clone = clone();
        clone.replace(placeholders);
        clone.send(receivers);
    }

    private int findSpaces(String text, boolean reverse) {
        char[] chars = text.toCharArray();
        if (reverse)
            reverseArray(chars);

        int found = 0;
        for (char chaz : chars) {
            if (chaz == ' ')
                found++;
            else
                return found;
        }
        return found;
    }

    private void reverseArray(char[] a) {
        int n = a.length;
        char i, k, t;
        for (i = 0; i < n / 2; i++) {
            t = a[i];
            a[i] = a[n - i - 1];
            a[n - i - 1] = t;
        }
    }

    public static enum InsertMethod {
        AFTER,
        BEFORE
    }

    @SneakyThrows
    public ChatLine clone() {
        ChatLine clone = new ChatLine();
        clone.centered = centered;
        clone.contentList = contentList.stream().map(LineContent::clone).collect(Collectors.toCollection(LinkedList::new));
        return clone;
    }

    public String raw() {
        return contentList.stream().map(LineContent::text).collect(Collectors.joining(" "));
    }

    public LineContent findContent(Predicate<LineContent> filter) {
        return contentList
                .stream()
                .filter(filter)
                .findFirst()
                .orElse(null);
    }

    public ChatLine removeContentIf(Predicate<LineContent> filter) {
        contentList.removeIf(filter);
        return this;
    }
}
