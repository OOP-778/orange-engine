package com.oop.orangeengine.message;

import com.oop.orangeengine.main.util.OActionBar;
import com.oop.orangeengine.main.util.OTitle;
import com.oop.orangeengine.message.impl.OActionBarMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.OTitleMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.LineContent;
import com.oop.orangeengine.message.impl.chat.addition.Addition;
import com.oop.orangeengine.message.impl.chat.addition.impl.ChatAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.CommandAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.HoverTextAddition;
import com.oop.orangeengine.message.impl.chat.addition.impl.SuggestionAddition;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import com.oop.orangeengine.yaml.interfaces.Valuable;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class YamlMessage {
    public static void save(OMessage message, String path, Valuable valuable) {
        if (message instanceof OChatMessage)
            Chat.save((OChatMessage) message, path, valuable);

        else if (message instanceof OActionBarMessage)
            save((OActionBarMessage) message, path, valuable);

        else if (message instanceof OTitleMessage)
            save((OTitleMessage) message, path, valuable);
    }

    public static OMessage load(ConfigSection section) {
        Optional<ConfigValue> optType = section.get("type");
        if (optType.isPresent()) {
            String type = optType.get().getAs(String.class);
            if (type.equalsIgnoreCase("chat"))
                return Chat.load(section);

            else if (type.equalsIgnoreCase("actionbar"))
                return loadActionBar(section);

            else if (type.equalsIgnoreCase("title"))
                return loadTitle(section);
            else
                return Chat.load(section);
        }

        return Chat.load(section);
    }

    public static OMessage load(String path, Valuable valuable) {
        Optional<ConfigSection> optionalSection = valuable.getSection(path);
        if (optionalSection.isPresent()) {
            return load(optionalSection.get());

        } else {
            Optional<ConfigValue> optionalValue = valuable.get(path);
            if (optionalValue.isPresent())
                return Chat.load(optionalValue.get());
        }
        return null;
    }

    private static OTitleMessage loadTitle(ConfigSection section) {
        section.ensureHasAny("title", "sub title");
        OTitleMessage titleMessage = new OTitleMessage();
        section.ifValuePresent("title", String.class, titleMessage::title);
        section.ifValuePresent("sub title", String.class, titleMessage::subTitle);
        section.ifValuePresent("fade in", int.class, titleMessage::fadeIn);
        section.ifValuePresent("stay", int.class, titleMessage::stay);
        section.ifValuePresent("fade out", int.class, titleMessage::fadeOut);
        return titleMessage;
    }

    private static OActionBarMessage loadActionBar(ConfigSection section) {
        OActionBarMessage actionBarMessage = new OActionBarMessage();
        section.ensureHasValues("text");

        actionBarMessage.text(section.getAs("text"));
        return actionBarMessage;
    }

    public static void save(OActionBarMessage message, String path, Valuable config) {
        ConfigSection section = config.createSection(path);
        section.set("type", "actionbar");
        section.set("text", message.text());
    }

    public static void save(OTitleMessage message, String path, Valuable config) {
        ConfigSection section = config.createSection(path);
        section.set("type", "title");
        if (message.title() != null)
            section.set("title", message.title());

        if (message.subTitle() != null)
            section.set("sub title", message.subTitle());

        section.set("fade in", message.fadeIn());
        section.set("stay", message.stay());
        section.set("fade out", message.fadeOut());
    }

    public static class Chat {
        public static void save(OChatMessage message, String path, Valuable valuable) {
            /*
            If message doesn't have any attributes like center and it's one lined.
            */
            if (!requiresSection(message)) {
                if (requiresSection(message.lineList().element())) {
                    ChatLine line = message.lineList().element();
                    ConfigSection section = valuable.createSection(path);
                    section.set("type", "chat");

                    save(line, section);

                } else
                    valuable.set(path, message.lineList().element().contentList().element().text());

            } else {
                if (!message.centered() && allOneLined(message)) {
                    valuable.set(path, message.lineList()
                            .stream()
                            .map(ChatLine::raw)
                            .collect(Collectors.toList()));

                } else {
                    ConfigSection section = valuable.createSection(path);
                    section.set("type", "chat");
                    if (message.centered())
                        section.set("center", true);

                    if (message.lineList().size() == 1) {
                        ChatLine line = message.lineList().element();
                        save(line, section);

                    } else {
                        ConfigSection linesSection = section.createSection("lines");

                        int i = 1;
                        for (ChatLine line : message.lineList()) {
                            if (requiresSection(line)) {
                                ConfigSection lineSection = linesSection.createSection(i + "");
                                save(line, lineSection);

                            } else linesSection.set(i + "", line.contentList().element().text());
                            i++;
                        }
                    }
                }
            }
        }

        private static void save(ChatLine line, ConfigSection section) {
            if (line.centered())
                section.set("center", true);

            if (line.contentList().size() == 1) {
                LineContent content = line.contentList().element();
                save(content, section);

            } else if (line.contentList().size() > 1) {
                ConfigSection allContentSection = section.createSection("content");

                int i = 1;
                for (LineContent content : line.contentList()) {
                    if (!requiresSection(content)) {
                        allContentSection.set(i + "", content.text());

                    } else {
                        ConfigSection contentSection = allContentSection.createSection(i + "");
                        save(content, contentSection);
                    }
                    i++;
                }
            }
        }

        private static void save(LineContent content, ConfigSection section) {
            section.set("text", content.text());

            for (Addition addition : content.additionList()) {
                if (addition instanceof CommandAddition)
                    section.set("command", ((CommandAddition) addition).command());
                else if (addition instanceof HoverTextAddition) {
                    section.set("hover", ((HoverTextAddition) addition).hoverText());
                } else if (addition instanceof SuggestionAddition)
                    section.set("suggestion", ((SuggestionAddition) addition).suggestion());
                else if (addition instanceof ChatAddition)
                    section.set("chat", ((ChatAddition)addition).message());
            }
        }

        private static boolean requiresSection(OChatMessage message) {
            if (isMultiLine(message) || message.centered()) return true;
            return requiresSection(message.lineList().element());
        }

        private static boolean requiresSection(ChatLine line) {
            if (line.contentList().size() > 1) return true;

            LineContent content = line.contentList().element();
            return requiresSection(content);
        }

        private static boolean requiresSection(LineContent content) {
            return !content.additionList().isEmpty();
        }

        private static boolean isMultiLine(OChatMessage message) {
            return message.lineList().size() > 1;
        }

        private static boolean allOneLined(OChatMessage message) {
            return message.lineList()
                    .stream()
                    .noneMatch(Chat::requiresSection);
        }

        public static OChatMessage load(ConfigSection section) {
            OChatMessage message = new OChatMessage();
            section.ifValuePresent("center", boolean.class, message::centered);

            if (section.isValuePresent("text")) {
                message.append(new ChatLine().append(loadContentLine(section)));
                return message;
            }

            Optional<ConfigSection> optContent = section.getSection("content");
            Optional<ConfigSection> optLines = section.getSection("lines");
            if (optContent.isPresent())
                return message.append(loadLine(optContent.get()));

            if (optLines.isPresent()) {
                ConfigSection linesSection = optLines.get();
                Map<Integer, ChatLine> lineMap = new HashMap<>();
                linesSection.getValues().forEach((key, value) -> {
                    if (!NumberUtils.isNumber(key)) return;

                    lineMap.put(Integer.parseInt(key), new ChatLine(new LineContent(value.getAs(String.class))));
                });

                linesSection.getSections().forEach((key, section2) -> {
                    if (!NumberUtils.isNumber(key)) return;

                    ConfigSection contentSection2 = section2.getSection("content").orElseThrow(() -> new IllegalStateException("Missing content section"));
                    if (contentSection2 == null)
                        lineMap.put(Integer.parseInt(key), new ChatLine(loadContentLine(section2)));

                    else
                        lineMap.put(Integer.parseInt(key), loadLine(contentSection2));
                });

                lineMap.keySet()
                        .stream()
                        .sorted()
                        .forEach(pos -> message.append(lineMap.get(pos)));
            }
            return message;
        }

        private static ChatLine loadLine(ConfigSection contentSection) {
            ChatLine line = new ChatLine();

            Map<Integer, LineContent> contentMap = new HashMap<>();
            contentSection.getValues().forEach((key, value) -> {
                if (!NumberUtils.isNumber(key)) return;

                contentMap.put(Integer.parseInt(key), new LineContent(value.getAs(String.class)));
            });

            contentSection.getSections().forEach((key, section) -> {
                if (!NumberUtils.isNumber(key)) return;

                contentMap.put(Integer.parseInt(key), loadContentLine(section));
            });

            contentMap.keySet()
                    .stream()
                    .sorted()
                    .forEach(pos -> line.append(contentMap.get(pos)));
            return line;
        }

        public static OChatMessage load(ConfigValue value) {
            if (value.isList())
                return new OChatMessage(value.getAsList(String.class));
            
            return new OChatMessage(((String) value.getObject()));
        }

        private static LineContent loadContentLine(ConfigSection section) {
            LineContent lineContent = new LineContent(section.getAs("text", String.class));
            section.ifValuePresent("command", String.class, command -> lineContent.command().command(command));
            section.ifValuePresent("chat", String.class, chat -> lineContent.chat().message(chat));
            section.ifValuePresent("hover", Object.class, hover -> {
                if (hover instanceof List)
                    lineContent.hover().set((List<String>) hover);
                else
                    lineContent.hover().add(hover.toString());
            });
            section.ifValuePresent("suggestion", String.class, suggestion -> lineContent.suggestion().suggestion(suggestion));
            return lineContent;
        }
    }
}
