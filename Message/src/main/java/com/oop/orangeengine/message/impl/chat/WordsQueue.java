package com.oop.orangeengine.message.impl.chat;

import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.oop.orangeengine.message.ChatUtil.parseHexColor;

@Getter
public class WordsQueue {

    public static List<OPair<Character, Character>> blacklistedCharsFromColoring = new ArrayList<OPair<Character, Character>>() {{

    }};

    private List<Word> words;
    private WordDecoration endDecoration;

    private WordsQueue(List<Word> words, WordDecoration decoration) {
        this.words = words;
        this.endDecoration = decoration;
    }

    public static WordsQueue of(String text) {
        return of(text, new WordDecoration());
    }

    @SneakyThrows
    public static WordsQueue of(String text, WordDecoration lastDecoration) {
        List<Word> words = new ArrayList<>();
        char[] textChars = text.toCharArray();
        WordDecoration decoration = lastDecoration == null ? new WordDecoration() : lastDecoration;
        OPair<Character, Character> blacklisted = null;

        // Translate native mc colors to bukkit
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < textChars.length; i++) {
            char character = textChars[i];

            if (blacklisted == null) {
                blacklisted = blacklistedCharsFromColoring
                        .stream()
                        .filter(pair -> pair.getFirst() == character)
                        .findFirst()
                        .orElse(null);

                if (blacklisted != null) {
                    builder.append(character);
                    continue;
                }
            } else {
                if (blacklisted.getSecond() == character)
                    blacklisted = null;

                builder.append(character);
                continue;
            }

            /*
            Because word is starting with a space, ignore the new word on space rule
            */
            if (builder.length() == 0 && character == ' ') {
                builder.append(character);
                decoration.apply(builder);
                continue;
            }

            /*
            Because the character is space and it isn't the start of the word, build the word
            */
            if (character == ' ') {
                if (builder.toString().toCharArray()[builder.length() - 1] == ' ') {
                    decoration.apply(builder);
                    builder.append(" ");
                    continue;
                }

                words.add(new Word(builder.toString(), decoration));
                builder = new StringBuilder();

                decoration.apply(builder);

                builder.append(character);
                continue;
            }

            /*
            Check for hex colors
            */
            if (character == '#' && OVersion.isOrAfter(16)) {
                String hex = getNextOrNull(Arrays.copyOfRange(textChars, i + 1, textChars.length), 6);
                if (hex != null) {
                    String parsed = parseHexColor(hex);
                    System.out.println("Parsed: " + parsed);
                    decoration.setColor(parsed);

                    OPair<String, Integer> whileMatches = getWhileMatches(Arrays.copyOfRange(textChars, i + 7, textChars.length), in -> in != '&' && in != '#' && in != ChatColor.COLOR_CHAR);
                    builder.append(parsed);
                    builder.append(whileMatches.getFirst());

                    i += 6 + whileMatches.getSecond();
                    continue;
                }
            }

            /*
            Check for color & decoration
            */
            if (character == '&' || character == '\u00a7') {
                char codeAfter = textChars[i + 1];
                ChatColor dec = ChatColor.getByChar(codeAfter);
                if (dec == null) {
                    builder.append(character);
                    continue;
                }

                System.out.println(dec.getName());
                WordsColor wordsColor = new WordsColor(dec.toString(), dec.name());

                if (wordsColor.isFormat()) {
                    decoration.addDecorator(dec);
                } else
                    decoration.setColor(dec);

                builder.append(character).append(codeAfter);
                i = i + 1;
                continue;
            }

            decoration.apply(builder);
            builder.append(character);
        }

        words.add(new Word(builder.toString(), decoration));
        return new WordsQueue(words, decoration);
    }

    @Getter
    @AllArgsConstructor
    public static class Word {

        private String string;
        private WordDecoration decoration;

    }

    public static class WordDecoration {
        private Set<WordsColor> decorations = new HashSet<>();
        private WordsColor color = new WordsColor(ChatColor.RESET.toString(), "RESET");

        public WordDecoration setColor(@NonNull ChatColor color) {
            clearDecorators();
            this.color = new WordsColor(color.toString(), color.name());
            return this;
        }

        public WordDecoration setColor(String color) {
            clearDecorators();
            this.color = new WordsColor(color, "none");
            return this;
        }

        public WordDecoration clearDecorators() {
            decorations.clear();
            return this;
        }

        public WordDecoration addDecorator(@NonNull ChatColor decorator) {
            decorations.add(new WordsColor(decorator.toString(), decorator.name()));
            return this;
        }

        public String buildColor() {
            return color == null ? "" : color.toString();
        }

        public String buildDecorators() {
            return decorations.isEmpty() ? "" : fromStream(decorations.stream().map(WordsColor::toString));
        }

        private String fromStream(Stream<String> stream) {
            StringBuilder builder = new StringBuilder();
            stream.forEach(builder::append);
            return builder.toString();
        }

        public void apply(StringBuilder builder) {
            List<WordsColor> colors = new ArrayList<>();
            if (color != null)
                colors.add(color);
            colors.addAll(decorations);
            builder.append(buildColor()).append(buildDecorators());
        }
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

    private static OPair<String, Integer> getWhileMatches(char[] array, Predicate<Character> filter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            char character = array[i];
            builder.append(character);
            if (!filter.test(character)) {
                return new OPair<>(builder.toString(), i);
            }
        }

        return new OPair<>(builder.toString(), array.length);
    }

    @AllArgsConstructor
    private static class WordsColor {
        private @NonNull String color;
        private @NonNull String colorName;

        public boolean isFormat() {
            try {
                org.bukkit.ChatColor chatColor = org.bukkit.ChatColor.valueOf(colorName);
                return chatColor.isFormat();
            } catch (Throwable throwable) {
                return false;
            }
        }

        @Override
        public String toString() {
            return color;
        }
    }
}
