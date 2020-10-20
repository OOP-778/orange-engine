package com.oop.orangeengine.message.impl.chat;

import com.google.common.base.Preconditions;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.message.Replaceable;
import com.oop.orangeengine.message.Sendable;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.color.ChatColor;
import com.oop.orangeengine.message.impl.chat.color.OChatColor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Accessors(chain = true, fluent = true)
public class ChatLine implements Replaceable<ChatLine>, Cloneable, Sendable {

    @Getter
    private InsertableList<LineContent> contentList = new InsertableList<>();

    @Getter
    @Setter
    private boolean centered = false;

    public ChatLine(String content) {
        this(LineContent.of(content));
    }

    public ChatLine(LineContent... content) {
        contentList.addAll(Arrays.asList(content));
    }

    public ChatLine insert(LineContent lineContent, LineContent at, InsertableList.InsertMethod method) {
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

    public int indexOf(LineContent content) {
        return contentList.indexOf(content);
    }

    public ChatLine insert(LineContent what, int at) {
        contentList.add(at, what);
        return this;
    }

    public ChatLine insert(int at, LineContent... content) {
        contentList.insert(at, content);
        return this;
    }

    public ChatLine replace(LineContent replace, LineContent to) {
        int i = contentList.indexOf(replace);
        Preconditions.checkArgument(i != -1, "Index cannot be -1");

        contentList.set(i, to);
        return this;
    }

    public ChatLine set(int index, LineContent content) {
        Preconditions.checkArgument(contentList.size() > index, "Index is incorrect");
        contentList.set(index, content);
        return this;
    }

    public ChatLine append(LineContent... content) {
        contentList.addAll(Arrays.asList(content));
        return this;
    }

    public ChatLine append(String content) {
        return append(LineContent.of(content));
    }

    public ChatLine replace(String key, ChatLine value) {
        for (LineContent lineContent : new LinkedList<>(this.contentList)) {
            if (!lineContent.text().contains(key)) continue;

            String[] split = lineContent.text().split(Pattern.quote(key));
            if (split.length == 2) {
                LineContent firstPart = new LineContent(split[0]);
                ChatLine secondPart = value.clone();
                LineContent thirdPart = new LineContent(split[1]);

                replace(lineContent, firstPart);
                int index = contentList.insert(firstPart, InsertableList.InsertMethod.REPLACE, secondPart.contentList.toArray(new LineContent[0]));
                insert(index, thirdPart);

            } else if (split.length == 1) {
                LineContent firstPart = new LineContent(split[0]);
                replace(lineContent, firstPart);
                contentList.insert(firstPart, InsertableList.InsertMethod.AFTER, value.clone().contentList.toArray(new LineContent[0]));

            } else
                contentList.insert(lineContent, InsertableList.InsertMethod.REPLACE, value.clone().contentList.toArray(new LineContent[0]));
        }
        return this;
    }

    public ChatLine replace(String key, OChatMessage message) {
        for (LineContent lineContent : new LinkedList<>(this.contentList)) {
            if (!lineContent.text().contains(key)) continue;

            String[] split = lineContent.text().split(Pattern.quote(key));
            if (split.length == 2) {
                LineContent firstPart = new LineContent(split[0]);
                OChatMessage secondPart = message.clone();
                LineContent thirdPart = new LineContent(split[1]);

                int index = insert(indexOf(lineContent), secondPart.lineList().toArray(new ChatLine[0]));
                replace(lineContent, firstPart);
                insert(index, thirdPart);

            } else if (split.length == 1) {
                LineContent firstPart = new LineContent(split[0]);
                insert(indexOf(lineContent), message.clone().lineList().toArray(new ChatLine[0]));
                replace(lineContent, firstPart);

            } else
                contentList.insert(lineContent, InsertableList.InsertMethod.REPLACE, message.clone().lineList().stream().flatMap(line -> line.contentList.stream()).toArray(LineContent[]::new));
        }
        return this;
    }

    private int insert(int indexOf, ChatLine... lines) {
        for (ChatLine chatLine : lines) {
            for (LineContent lineContent : chatLine.contentList) {
                contentList.add(indexOf += 1, lineContent);
            }
        }
        return indexOf;
    }

    public ChatLine replace(String key, LineContent content) {
        for (LineContent lineContent : new LinkedList<>(this.contentList)) {
            if (!lineContent.text().contains(key)) continue;

            String[] split = lineContent.text().split(Pattern.quote(key));
            if (split.length == 2) {
                LineContent firstPart = new LineContent(split[0]);
                LineContent secondPart = content.clone();
                LineContent thirdPart = new LineContent(split[1]);

                replace(lineContent, firstPart);
                insert(secondPart, firstPart, InsertableList.InsertMethod.AFTER);
                insert(thirdPart, secondPart, InsertableList.InsertMethod.AFTER);

            } else if (split.length == 1) {
                LineContent firstPart = new LineContent(split[0]);
                replace(lineContent, firstPart);
                insert(content.clone(), firstPart, InsertableList.InsertMethod.AFTER);
            } else
                contentList.insert(lineContent, InsertableList.InsertMethod.REPLACE, content.clone());
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
    public ChatLine replace(@NonNull Function<String, String> function) {
        contentList.forEach(content -> content.replace(function));
        return this;
    }

    @Override
    public ChatLine returnThis() {
        return this;
    }

    public TextComponent createComponent() {
        StringBuilder appendStart = new StringBuilder(), appendEnd = new StringBuilder();
        if (centered) {
            String content = Centered.getCenteredMessage(raw());
            IntStream.range(1, findSpaces(content, false)).forEach(i -> appendStart.append(" "));
            IntStream.range(1, findSpaces(content, true)).forEach(i -> appendEnd.append(" "));
        }

        List<BaseComponent> components = buildComponents();
        TextComponent base = new TextComponent(appendStart.toString());
        components.forEach(base::addExtra);
        base.addExtra(appendEnd.toString());
        return base;
    }

    @Override
    public void send(CommandSender... receivers) {
        if (Arrays.stream(receivers).noneMatch(receiver -> receiver instanceof Player)) {
            String raw = Helper.color(raw());
            for (CommandSender receiver : receivers)
                receiver.sendMessage(raw);

        } else {
            TextComponent component = createComponent();
            for (CommandSender receiver : receivers) {
                if (receiver instanceof Player)
                    ((Player) receiver).spigot().sendMessage(component);

                else
                    receiver.sendMessage(Helper.color(component.toLegacyText()));
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

    public void append(ChatLine parentLine) {
        contentList.addAll(parentLine.contentList);
    }

    @SneakyThrows
    public ChatLine clone() {
        ChatLine clone = new ChatLine();
        clone.centered = centered;
        clone.contentList = contentList.stream().map(LineContent::clone).collect(Collectors.toCollection(InsertableList::new));
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

    public List<BaseComponent> buildComponents() {
        StringBuilder builder = new StringBuilder();
        List<BaseComponent> components = new ArrayList<>();

        ComponentDecoration decoration = new ComponentDecoration();
        for (LineContent lineContent : contentList) {
            @NonNull String text = lineContent.text();

            List<TextComponent> contentComponents = new ArrayList<>();

            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char character = chars[i];

                // Check for hex Colors
                if (character == '#' && OVersion.isOrAfter(16)) {
                    String hex = getNextOrNull(Arrays.copyOfRange(chars, i + 1, chars.length), 6);
                    if (hex != null) {
                        ChatColor parsed = OChatColor.match("#" + hex);

                        if (i != 0) {
                            if (chars.length > i + 6 && chars[i + 6] == '&') {
                                i += 6;
                                continue;
                            }

                            TextComponent component = new TextComponent(builder.toString());
                            builder = new StringBuilder();
                            decoration.apply(component);
                            contentComponents.add(component);
                        }

                        decoration.setColor(parsed);
                        i += 6;
                        continue;
                    }
                }

                // Check for bukkit colors
                if (character == '&' || character == '\u00a7') {
                    char codeAfter = chars[i + 1];
                    ChatColor color = OChatColor.match(String.valueOf(codeAfter));
                    if (color == null) {
                        i += 1;
                        continue;
                    }

                    if (i != 0) {
                        if (chars.length > i + 1 && chars[i + 1] == '&') {
                            i += 1;
                            continue;
                        }
                        TextComponent component = new TextComponent(builder.toString());
                        builder = new StringBuilder();
                        decoration.apply(component);
                        contentComponents.add(component);
                    }

                    if (color.isFormat())
                        decoration.decorations().add(color);

                    else {
                        decoration.setColor(color);
                    }

                    i += 1;
                    continue;
                }

                builder.append(character);
            }

            TextComponent component = new TextComponent(builder.toString());
            builder = new StringBuilder();
            decoration.apply(component);
            contentComponents.add(component);

            contentComponents.forEach(comp -> lineContent.additionList().forEach(addition -> addition.apply(comp)));
            components.addAll(contentComponents);
        }

        return components;
    }

    private static String getNextOrNull(char[] array, int amount) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            if (i == amount)
                return builder.toString();

            char character = array[i];
            builder.append(character);
        }

        return null;
    }

    @Getter
    private class ComponentDecoration {
        private ChatColor color = OChatColor.match("RESET");
        private List<ChatColor> decorations = new ArrayList<>();

        public void applyColor(TextComponent component) {
            component.setColor((net.md_5.bungee.api.ChatColor) color.getColorObject());
        }

        public void apply(TextComponent component) {
            applyColor(component);
            applyDecor(component);
        }

        public void applyDecor(TextComponent component) {
            for (ChatColor decoration : decorations) {
                if (decoration.getName().contentEquals("BOLD"))
                    component.setBold(true);
                else if (decoration.getName().contentEquals("UNDERLINE"))
                    component.setUnderlined(true);
                else if (decoration.getName().contentEquals("ITALIC"))
                    component.setItalic(true);
                else if (decoration.getName().contentEquals("MAGIC"))
                    component.setObfuscated(true);
            }
        }

        public void setColor(ChatColor color) {
            decorations.clear();
            this.color = color;
        }
    }
}
