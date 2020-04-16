package com.oop.orangeengine.message;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WordsQueue {

    public static List<OPair<Character, Character>> blacklistedCharsFromColoring = new ArrayList<OPair<Character, Character>>(){{

    }};

    private List<Word> words;
    private WordDecoration endDecoration;

    private WordsQueue(List<Word> words, WordDecoration decoration) {
        this.words = words;
        this.endDecoration = decoration;
    }

    public static WordsQueue of(String text) {
        return of(text, new WordDecoration(null, null));
    }

    public static WordsQueue of(String text, @NonNull WordDecoration lastDecoration) {
        List<Word> words = new ArrayList<>();
        char[] textChars = text.toCharArray();
        WordDecoration decoration = lastDecoration;
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
                if (decoration != null) {
                    if (decoration.getColor() != null)
                        builder.append(decoration.getColor().toString());

                    if (decoration.getDecoration() != null)
                        builder.append(decoration.getDecoration().toString());
                }
                continue;
            }

            /*
            Because the character is space and it isn't the start of the word, build the word
            */
            if (character == ' ') {
                words.add(new Word(builder.toString(), decoration));
                builder = new StringBuilder();

                if (decoration != null) {
                    if (decoration.getColor() != null)
                        builder.append(decoration.getColor().toString());

                    if (decoration.getDecoration() != null)
                        builder.append(decoration.getDecoration().toString());
                }

                builder.append(character);
                continue;
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

                if (dec.isFormat()) {
                    decoration.setDecoration(dec);
                }

                if (dec.isColor()) {
                    decoration.setColor(dec);
                    decoration.setDecoration(null);
                }

                builder.append(character).append(codeAfter);
                i = i + 1;
                continue;
            }

            if (decoration.getColor() != null)
                builder.append(decoration.getColor().toString());

            if (decoration.getDecoration() != null)
                builder.append(decoration.getDecoration().toString());
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

    @AllArgsConstructor
    @Getter
    @Setter
    public static class WordDecoration {
        private ChatColor decoration;
        private ChatColor color;
    }

}
