package com.oop.orangeengine.message;

import com.oop.orangeengine.message.additions.action.CommandAddition;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.value.AConfigurationValue;
import org.apache.commons.lang.math.NumberUtils;

import java.util.*;
import java.util.stream.Collectors;

public class YamlMessage {

    /*
    SAVING PART
    */
    public static void save(OMessage message, String path, OConfiguration configuration, String... description) {
        /*
        If message doesn't have any attributes like center and it's one lined.
        */
        if (!requiresSection(message)) {
            if (requiresSection(message.getLineList().element())) {
                MessageLine line = message.getLineList().element();
                ConfigurationSection section = configuration.createNewSection(path);
                section.description(new ArrayList<>(Arrays.asList(description)));

                save(line, section);

            } else
                configuration.setValue(path, message.getLineList().element().contentList().element().getText()).description(new ArrayList<>(Arrays.asList(description)));
        } else {
            if (!message.isCenter() && allOneLined(message)) {
                configuration.setValue(path, message.getLineList()
                        .stream()
                        .map(MessageLine::getRaw)
                        .collect(Collectors.toList()))
                        .description(new ArrayList<>(Arrays.asList(description)));

            } else {
                ConfigurationSection section = configuration.createNewSection(path);
                section.description(new ArrayList<>(Arrays.asList(description)));
                if (message.isCenter())
                    section.setValue("center", true);

                if (message.getLineList().size() == 1) {
                    MessageLine line = message.getLineList().element();
                    save(line, section);

                } else {
                    ConfigurationSection linesSection = section.createNewSection("lines");

                    int i = 1;
                    for (MessageLine line : message.getLineList()) {
                        if (requiresSection(line)) {
                            ConfigurationSection lineSection = linesSection.createNewSection(i + "");
                            save(line, lineSection);

                        } else linesSection.setValue(i + "", line.contentList().element().getText());
                        i++;
                    }
                }
            }
        }
    }

    private static void save(MessageLine line, ConfigurationSection section) {
        if (line.autoSpaces())
            section.setValue("auto spaces", true);

        if (line.center())
            section.setValue("center", true);

        if (line.contentList().size() == 1) {
            LineContent content = line.contentList().element();
            save(content, section);

        } else if (line.contentList().size() > 1) {
            ConfigurationSection allContentSection = section.createNewSection("content");

            int i = 1;
            for (LineContent content : line.contentList()) {
                if (!requiresSection(content)) {
                    allContentSection.setValue(i + "", content.getText());

                } else {
                    ConfigurationSection contentSection = allContentSection.createNewSection(i + "");
                    save(content, contentSection);
                }

                i++;
            }
        }
    }

    private static void save(LineContent content, ConfigurationSection section) {
        section.setValue("text", content.getText());

        // Set hover text
        if (!content.getHoverText().isEmpty()) {
            if (content.getHoverText().size() == 1)
                section.setValue("hover", content.getHoverText().get(0));

            else
                section.setValue("hover", content.getHoverText());
        }

        // Save Additions
        content.getAdditionList()
                .stream()
                .filter(addition -> addition instanceof CommandAddition)
                .findFirst()
                .ifPresent(addition -> section.setValue("command", ((CommandAddition) addition).getCommand()));
    }

    private static boolean requiresSection(OMessage message) {
        if (isMultiLine(message) || message.isCenter()) return true;
        return requiresSection(message.getLineList().element());
    }

    private static boolean requiresSection(MessageLine line) {
        if (line.contentList().size() > 1) return true;

        LineContent content = line.contentList().element();
        return !content.getHoverText().isEmpty() || content.getAdditionList().stream().anyMatch(addition -> addition instanceof CommandAddition);
    }

    private static boolean requiresSection(LineContent content) {
        return !content.getHoverText().isEmpty() || !content.getAdditionList().isEmpty();
    }

    private static boolean isMultiLine(OMessage message) {
        return message.getLineList().size() > 1;
    }

    private static boolean allOneLined(OMessage message) {
        return message.getLineList()
                .stream()
                .noneMatch(YamlMessage::requiresSection);
    }

    /*
    LOADING PART
    */
    public static OMessage load(ConfigurationSection section) {
        OMessage message = new OMessage();
        loadSection(section, message);
        return message;
    }

    private static void loadSection(ConfigurationSection section, OMessage message) {
        section.ifValuePresent("center", boolean.class, message::setCenter);
        ConfigurationSection linesSection = section.getSection("lines");
        ConfigurationSection contentSection = section.getSection("content");
        if (contentSection != null)
            message.appendLine(loadLine(section, contentSection));

        else if (linesSection != null) {
            Map<Integer, MessageLine> lineMap = new HashMap<>();
            linesSection.getValues().forEach((key, value) -> {
                if (!NumberUtils.isNumber(key)) return;

                lineMap.put(Integer.parseInt(key), new MessageLine().append(new LineContent(value.getValueAsReq())));
            });

            linesSection.getSections().forEach((key, section2) -> {
                if (!NumberUtils.isNumber(key)) return;

                ConfigurationSection contentSection2 = section2.getSection("content");
                if (contentSection2 == null)
                    lineMap.put(Integer.parseInt(key), new MessageLine().append(loadContentLine(section2)));

                else
                    lineMap.put(Integer.parseInt(key), loadLine(section2, contentSection2));
            });

            lineMap.keySet()
                    .stream()
                    .sorted()
                    .forEach(pos -> message.appendLine(lineMap.get(pos)));
        } else
            message.appendLine(new MessageLine().append(loadContentLine(section)));
    }

    public static OMessage load(OConfiguration configuration, String path) {
        OMessage message = new OMessage();
        ConfigurationSection section = configuration.getSection(path);

        if (section == null) {
            AConfigurationValue value = configuration.getValue(path);
            if (value == null)
                throw new IllegalStateException("Failed to load message in " + configuration.getOFile().getFileName().replace(".yml", "") + " path " + path + " because the value is not found!");

            // We got multi line text
            if (value.getValue() instanceof List)
                ((List<String>) value.getValue()).forEach(message::appendLine);

            else message.appendLine(value.getValueAsReq(String.class));

        } else
            loadSection(section, message);

        return message;
    }

    private static MessageLine loadLine(ConfigurationSection parentSection, ConfigurationSection contentSection) {
        MessageLine line = new MessageLine();
        parentSection.ifValuePresent("auto spaces", boolean.class, line::autoSpaces);

        Map<Integer, LineContent> contentMap = new HashMap<>();
        contentSection.getValues().forEach((key, value) -> {
            if (!NumberUtils.isNumber(key)) return;

            contentMap.put(Integer.parseInt(key), new LineContent(value.getValueAsReq()));
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

    private static LineContent loadContentLine(ConfigurationSection section) {
        LineContent lineContent = new LineContent(section.getValueAsReq("text"));
        Optional.ofNullable(section.getValue("hover")).ifPresent(hover -> {
            if (hover.getValue() instanceof List)
                lineContent.setHoverText((List<String>) hover.getValue());

            else
                lineContent.appendHover(hover.getValueAsReq());
        });

        section.ifValuePresent("command", String.class, command -> lineContent.addAddition(new CommandAddition(command)));
        return lineContent;
    }

}
