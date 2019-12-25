package com.oop.orangeengine.yaml.util;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.mapper.ObjectsMapper;
import com.oop.orangeengine.yaml.value.AConfigurationValue;
import com.oop.orangeengine.yaml.value.ConfigurationList;
import com.oop.orangeengine.yaml.value.ConfigurationValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class ConfigurationUtil {

    public static String stringWithSpaces(int spaces) {
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, spaces).forEach(s -> builder.append(" "));
        return builder.toString();
    }

    public static int findSectionEnd(int sectionStart, OIterator<UnreadString> iterator) {

        UnreadString[] array = iterator.getObjectsCopy(UnreadString.class);

        int startingSpaces = -1;
        int lastIndex = 0;

        for (int index = sectionStart; index < array.length; index++) {

            OPair<String, Integer> line = parse(array[index].value());
            if (startingSpaces == -1) {
                startingSpaces = line.getSecond();
                continue;
            }

            if (line.getSecond() <= startingSpaces && line.getFirst().replaceAll("\\s+", "").length() > 0)
                return index - 1;

            lastIndex = index;

        }

        return lastIndex;

    }

    public static ConfigurationSection loadSection(OConfiguration configuration, OIterator<UnreadString> iterator) {

        ConfigurationSection leadSection = null;
        ConfigurationSection currentSection = null;
        List<String> description = new ArrayList<>();

        while (iterator.hasNext()) {
            UnreadString line = iterator.next();
            if (line == null) continue;

            if (line.value().contains(":") && !line.value().contains("#")) {

                String[] split = splitAtFirst(line.value(), ':');
                if (split.length == 1) {

                    //Now we have to check if the line below is a list or not
                    UnreadString[] valuesArray = iterator.getObjectsCopy(UnreadString.class);
                    int elementIndex = Arrays.asList(valuesArray).indexOf(line);
                    boolean valid = isValidIndex(valuesArray, elementIndex + 1);

                    if (valid && isList(valuesArray, elementIndex + 1)) {

                        //IS LIST
                        List<UnreadString> listValues = iterator.nextValuesThatMatches(us -> us.value().contains("-"), true);
                        OPair<String, Integer> parsedKey = parse(split[0]);
                        ConfigurationList value = new ConfigurationList(parsedKey.getFirst(), listValues.stream().map(UnreadString::value).map(string -> parse(parse(string).getFirst().substring(1)).getFirst()).collect(toList()));

                        value.setSpaces(parsedKey.getSecond());
                        value.description(description);

                        if (currentSection != null) {

                            if (currentSection.getSpaces() >= value.getSpaces()) {

                                //We need to find setParent that has less setSpaces and the different is between 1-4
                                ConfigurationSection section = currentSection.getAllParents().stream().
                                        filter(s -> s.getSpaces() < value.getSpaces()).
                                        filter(s -> (value.getSpaces() - s.getSpaces()) <= 4).findFirst().orElse(null);

                                if (section != null) {
                                    currentSection = section;
                                    currentSection.assignValue(value);
                                }

                            } else {

                                currentSection.assignValue(value);

                            }
                        }

                    } else {

                        //IS SECTION
                        OPair<String, Integer> pair = parse(split[0]);
                        ConfigurationSection newSection = new ConfigurationSection(configuration, pair.getFirst(), pair.getSecond());
                        newSection.description(description);

                        if (leadSection == null) leadSection = newSection;
                        else currentSection.findAcceptableParent(newSection).assignSection(newSection);

                        currentSection = newSection;

                    }

                } else {

                    OPair<String, Integer> parsedKey = parse(split[0]);
                    AConfigurationValue value;

                    //Check for list
                    if (split[1].trim().startsWith("[]"))
                        value = new ConfigurationList(parsedKey.getFirst(), new ArrayList<>());

                    else
                        value = new ConfigurationValue(parsedKey.getFirst(), ObjectsMapper.mapObject(ConfigurationUtil.parse(split[1]).getFirst()));


                    value.setSpaces(parsedKey.getSecond());
                    value.description(description);

                    if (currentSection != null) {

                        if (currentSection.getSpaces() >= value.getSpaces()) {

                            //We need to find setParent that has less setSpaces and the different is between 1-4
                            ConfigurationSection section = currentSection.getAllParents().stream().
                                    filter(s -> s.getSpaces() < value.getSpaces()).
                                    filter(s -> (value.getSpaces() - s.getSpaces()) <= 4).findFirst().orElse(null);

                            if (section != null) {
                                currentSection = section;
                                currentSection.assignValue(value);
                            }

                        } else {

                            currentSection.assignValue(value);

                        }
                    }

                }

            } else {

                if (line.value().contains("#")) {
                    OPair<String, Integer> parsed = parse(line.value().substring(1));
                    if (parsed.getFirst().equalsIgnoreCase("------------------")) continue;

                    description.add(parsed.getFirst());

                } else if (line.value().trim().length() == 0 && !description.isEmpty()) description.add("");

            }

        }

        return leadSection;

    }

    public static int findSpaces(String string) {

        int count = 0;
        for (Character c : string.toCharArray()) {

            if (c.toString().equalsIgnoreCase(" ")) count++;
            else return count;

        }

        return count;

    }

    public static <T> List<T> copy(T[] array, int startIndex, int endIndex) {

        List<T> list = new ArrayList<>();

        for (int index = startIndex; index < array.length; index++) {

            list.add(array[index]);
            if (index == endIndex) break;

        }

        return list;

    }

    public static OPair<String, Integer> parse(String key) {

        int spaces = 0;
        StringBuilder builder = new StringBuilder();
        int lcount = 0;

        boolean foundFirstChar = false;

        for (Character c : key.toCharArray()) {

            if ((c.toString().equalsIgnoreCase(" ") || c.toString().equalsIgnoreCase("#")) && !foundFirstChar) spaces++;
            else {

                if (lcount == 0 && c.toString().equalsIgnoreCase("\"")) continue;
                if (lcount == key.substring(spaces).length() - 2 && c.toString().equalsIgnoreCase("\"")) continue;
                builder.append(c);
                foundFirstChar = true;
                lcount++;
            }

        }

        return new OPair<>(builder.toString(), spaces);

    }

    public static <T> boolean isValidIndex(T[] array, int index) {
        return array.length > index;
    }

    public static <T> boolean filter(T[] array, int startingIndex, Predicate<T> filter) {

        int valuesFound = 0;

        for (int index = startingIndex; index < array.length; index++) {

            T value = array[index];
            if (value.toString().contains("#") || value.toString().trim().length() == 0) continue;

            if (parse(value.toString()).getFirst().contains(":")) return false;

            boolean toReturn = filter.test(value);
            if (toReturn && parse(value.toString()).getFirst().contains(":")) return false;
            if (toReturn) return true;

        }

        return false;

    }

    public static boolean isList(UnreadString[] array, int startingIndex) {

        for (int index = startingIndex; index < array.length; index++) {

            UnreadString value = array[index];

            //We have to test the getSecond if it starts with '-' after all setSpaces is removed.
            //If it starts not with '-' it's not a list, but we must ignore white setSpaces & comments

            //Checking if string is a white space
            if (value.value().trim().length() == 0) continue;

            //Getting the first character after all the setSpaces
            String firstChar = firstCharsAfterSpaces(value.toString(), 1);

            //Checking if it's a comment if so continueing
            if (firstChar.equalsIgnoreCase("#")) continue;

            //We have found a list getSecond!
            return firstChar.equalsIgnoreCase("-");

        }

        return false;

    }

    public static String firstCharsAfterSpaces(String text, int charsAmount) {

        StringBuilder builder = new StringBuilder();
        int charsFound = 0;

        for (Character c : text.toCharArray()) {

            if (!c.toString().equalsIgnoreCase(" ")) {
                charsFound++;
                builder.append(c);
                if (charsAmount == charsFound) return builder.toString();
            }

        }

        return "";

    }

    public static void smartNewLine(CustomWriter bw) throws IOException {

        String lastWritten = bw.getLastWritten();
        if (lastWritten.length() <= 1) return;

        if (lastWritten.charAt(lastWritten.length() - 1) != ':')
            bw.newLine();

    }


    public static String[] splitAtFirst(String string, char character) {
        StringBuffer[] stringBuffers = new StringBuffer[]{new StringBuffer(), new StringBuffer()};
        boolean isFirst = true;

        for (char character2 : string.toCharArray()) {
            if (isFirst && character == character2) {
                isFirst = false;
                continue;
            }

            stringBuffers[isFirst ? 0 : 1].append(character2);
        }

        int firstLenght = stringBuffers[0].toString().trim().length();
        int secondLenght = stringBuffers[1].toString().trim().length();

        if (firstLenght > 0 && secondLenght > 0)
            return new String[]{stringBuffers[0].toString(), stringBuffers[1].toString()};

        else if (firstLenght > 0)
            return new String[]{stringBuffers[0].toString()};

        else
            return new String[]{stringBuffers[1].toString()};
    }

}
