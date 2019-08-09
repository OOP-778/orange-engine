package com.oop.orangeEngine.message;

import com.oop.orangeEngine.message.additions.action.CommandAddition;
import com.oop.orangeEngine.message.line.LineContent;
import com.oop.orangeEngine.message.line.MessageLine;
import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.OConfiguration;

import java.util.*;

public class YamlMessage {

    public static void saveToConfig(OMessage message, OConfiguration configuration, String path) {

        if (message.getLineList().size() <= 1) {

            //We got one line
            MessageLine line = message.getLineList().get(0);
            if (line.contentList().size() == 1) {
                LineContent lineContent = line.contentList().get(0);
                if (lineContent.getAdditionList().isEmpty() && lineContent.getHoverText() == null) {

                    //Simple set (as a getValue)
                    configuration.setValue(path, lineContent.getText());

                } else
                    saveLine(configuration.createNewSection(path), lineContent);
            } else {

                ConfigurationSection messageSection = configuration.createNewSection(path);
                messageSection.setValue("center", message.isCenter());

                ConfigurationSection contentSection = messageSection.createNewSection("content");
                int index = 1;
                for (LineContent lineContent : line.contentList()) {

                    ConfigurationSection textSection = contentSection.createNewSection(index + "");
                    saveLine(textSection, lineContent);
                    index++;

                }

            }

        } else {
            ConfigurationSection mainSection = configuration.createNewSection(path);
            ConfigurationSection linesSection = mainSection.createNewSection("lines");

            if (message.isCenter())
                mainSection.setValue("center", true);

            int currentLine = 1;
            for (MessageLine line : message.getLineList()) {

                ConfigurationSection lineSection = linesSection.createNewSection(currentLine + "");

                if (line.contentList().size() == 1) {
                    LineContent lineContent = line.contentList().get(0);
                    if (lineContent.getAdditionList().isEmpty() && lineContent.getHoverText() == null) {

                        //Simple set (as a getValue)
                        lineSection.setValue("text", lineContent.getText());

                    } else
                        saveLine(lineSection, lineContent);
                } else {
                    ConfigurationSection contentSection = lineSection.createNewSection("content");
                    int index = 1;
                    for (LineContent lineContent : line.contentList()) {

                        ConfigurationSection textSection = contentSection.createNewSection(index + "");
                        saveLine(textSection, lineContent);
                        index++;

                    }
                }
                currentLine++;

            }
        }

    }



    private static void saveLine(ConfigurationSection section, LineContent lineContent) {

        section.setValue("text", lineContent.getText());
        if (lineContent.getHoverText() != null)
            section.setValue("hover", lineContent.getHoverText());

        lineContent.getAdditionList().stream().
                filter(o -> o instanceof CommandAddition).
                map(o -> (CommandAddition) o).
                findFirst().
                ifPresent(cmdAddition -> section.setValue("runCommand", cmdAddition.getCommand()));
    }

    private static LineContent initContent(ConfigurationSection section) {

        LineContent lineContent = new LineContent(section.getValueAsReq("text"));

        if (section.isPresentValue("hover")) lineContent.hoverText(section.getValueAsReq("hover"));
        if (section.isPresentValue("runCommand"))
            lineContent.addAddition(new CommandAddition(section.getValueAsReq("runCommand")));

        return lineContent;
    }

    private static MessageLine initLine(ConfigurationSection section) {

        MessageLine messageLine = new MessageLine();
        messageLine.autoSpaces(true);
        if (section.isPresentValue("text"))

            //Single Content = Single LineContent
            messageLine.append(initContent(section));

        else if (section.isPresentSection("content")) {

            Map<Integer, LineContent> lineContentMap = new HashMap<>();
            for (ConfigurationSection contentSection : section.getSection("content").getSections().values()) {

                int place = Integer.parseInt(contentSection.getKey());
                lineContentMap.put(place, initContent(contentSection));

            }

            List<Integer> places = new ArrayList<>(lineContentMap.keySet());
            Collections.sort(places);

            places.forEach(place -> messageLine.append(lineContentMap.get(place)));

        } else {
            //Throw
        }

        return messageLine;

    }

    public static OMessage fromConfiguration(OConfiguration configuration, String path) {

        ConfigurationSection section = configuration.getSection(path);
        if(section == null) return null;

        return fromSection(section);

    }

    public static OMessage fromSection(ConfigurationSection section) {

        OMessage message = new OMessage();
        if (section.isPresentValue("center"))
            message.setCenter(section.getValueAsReq("center"));

        if (section.isPresentSection("lines")) {

            //Multiple Lines
            Map<Integer, MessageLine> messageLineMap = new HashMap<>();
            for (ConfigurationSection lineSection : section.getSection("lines").sections().values()) {

                int place = Integer.parseInt(lineSection.getKey());
                messageLineMap.put(place, initLine(lineSection));

            }
            messageLineMap.forEach((k, v) -> message.appendLine(v));

        } else
            message.appendLine(initLine(section));

        return message;

    }

}
