package com.oop.orangeengine.message.impl;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.MessageType;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.InsertableList;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Accessors(chain = true, fluent = true)
public class OChatMessage implements OMessage<OChatMessage> {

    @Getter
    private InsertableList<ChatLine> lineList = new InsertableList<>();

    @Getter
    private boolean centered = false;

    public OChatMessage() {}

    public OChatMessage(ChatLine ...lines) {
        lineList.addAll(Arrays.asList(lines));
    }

    public OChatMessage(String ...lines) {
        this(Arrays.asList(lines));
    }

    public OChatMessage(LineContent ...content) {
        lineList.add(new ChatLine(content));
    }

    public OChatMessage(Collection<String> lines) {
        lineList = lines.stream().map(ChatLine::new).collect(Collectors.toCollection(InsertableList::new));
    }

    @Override
    public String[] raw() {
        return lineList.stream().map(ChatLine::raw).toArray(String[]::new);
    }

    @Override
    public OChatMessage clone() {
        OChatMessage clone = new OChatMessage();
        clone.centered = centered;
        clone.lineList = lineList.stream().map(ChatLine::clone).collect(Collectors.toCollection(InsertableList::new));
        return clone;
    }

    public OChatMessage append(ChatLine line) {
        lineList.add(line);
        return this;
    }

    public OChatMessage append(String line) {
        lineList.add(new ChatLine(line));
        return this;
    }

    @Override
    public MessageType type() {
        return MessageType.CHAT;
    }

    @Override
    public void send(CommandSender ...receivers) {
        lineList.forEach(line -> line.send(receivers));
    }

    @Override
    public OChatMessage replace(Map<String, Object> placeholders) {
        lineList.forEach(line -> line.replace(placeholders));
        return this;
    }

    public OChatMessage replace(String key, LineContent content) {
        lineList.forEach(line -> line.replace(key, content));
        return this;
    }

    public OChatMessage replace(String key, OChatMessage message) {
        lineList.forEach(line -> line.replace(key, message));
        return this;
    }

    public OChatMessage replace(String key, ChatLine line) {
        lineList.forEach(line2 -> line2.replace(key, line));
        return this;
    }

    @Override
    public <E> OChatMessage replace(@NonNull E object, @NonNull Set<OPair<String, Function<E, String>>> placeholders) {
        lineList.forEach(line -> line.replace(object, placeholders));
        return this;
    }

    @Override
    public OChatMessage replace(@NonNull Function<String, String> function) {
        lineList.forEach(line -> line.replace(function));
        return this;
    }

    @Override
    public OChatMessage returnThis() {
        return this;
    }

    public ChatLine findLine(Predicate<ChatLine> lineFilter) {
        return lineList
                .stream()
                .filter(line -> lineFilter == null || lineFilter.test(line))
                .findFirst()
                .orElse(null);
    }

    public OPair<ChatLine, LineContent> findContent(Predicate<LineContent> contentFilter) {
        return lineList
                .stream()
                .map(line -> new OPair<>(line, line.findContent(contentFilter)))
                .filter(pair -> pair.getSecond() != null)
                .findFirst()
                .orElse(null);
    }

    public int indexOf(Predicate<ChatLine> filter) {
        return lineList.stream().filter(filter).findFirst().map(lineList::indexOf).orElse(-1);
    }

    public OChatMessage removeLineIf(Predicate<ChatLine> filter) {
        lineList.removeIf(filter);
        return this;
    }

    public OChatMessage removeContentIf(Predicate<LineContent> filter) {
        lineList.forEach(line -> line.contentList().removeIf(filter));
        return this;
    }

    @Override
    public void send(Map<String, Object> placeholders, CommandSender... receivers) {
        OChatMessage clone = clone();
        clone.replace(placeholders);
        clone.send(receivers);
    }

    public OChatMessage centered(boolean centered) {
        this.centered = centered;
        lineList.forEach(line -> line.centered(centered));
        return this;
    }

    public List<TextComponent> crateTextComponents() {
        List<TextComponent> components = new ArrayList<>();
        for (ChatLine chatLine : lineList) {
            components.add(chatLine.createComponent());
        }
        return components;
    }

    public OChatMessage append(OChatMessage message) {
        lineList.addAll(message.lineList);
        return this;
    }

    public OChatMessage insert(int index, OChatMessage message, InsertableList.InsertMethod method) {
        lineList.insert(index, method, message.lineList.toArray(new ChatLine[0]));
        return this;
    }

    public LineContent lastContent() {
        return lineList.getLast().contentList().getLast();
    }

}
