package com.oop.orangeengine.yaml.util;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.Config;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Commentator {

    @SneakyThrows
    public Commentator(Config config) {
        LineIterator iterator = new LineIterator(config.getFile().getFile());

        Map<String, List<String>> valuesComments = new LinkedHashMap<>();
        Map<String, List<String>> sectionComments = new LinkedHashMap<>();

        List<String> comments = new ArrayList<>();
        boolean header = false;

        ConfigPath path = new ConfigPath();
        while (iterator.next()) {
            OPair<String, Integer> trim = trim(iterator.get());
            String trimLine = trim.getFirst();

            if (trimLine.contentEquals("#head")) {
                header = true;
                continue;
            }

            if (header && trimLine.equalsIgnoreCase("#/head")) {
                config.getComments().addAll(new ArrayList<>(comments));
                comments.clear();
                header = false;
                continue;
            }

            if (trimLine.startsWith("#"))
                comments.add(trim(trimLine.substring(1)).getFirst());

            if (trimLine.contains(":")) {
                // We have found a section
                String[] trimSplit = trimLine.split(":");
                if (trimSplit.length == 1 || trimSplit[1].trim().length() == 0) {
                    String[] next = iterator.getNext(1);

                    // We have found a list
                    if (next.length > 0 && next[0].trim().startsWith("-")) {
                        path.setValue(trim);
                        if (!comments.isEmpty()) {
                            valuesComments.put(getPath(path, trimSplit[0]), new ArrayList<>(comments));
                            comments.clear();
                        }
                        continue;
                    }

                    path.setSection(trimSplit[0], trim);
                    if (!comments.isEmpty()) {
                        sectionComments.put(path.getPath(), new ArrayList<>(comments));
                        comments.clear();
                    }
                    continue;
                }

                path.setValue(trim);

                // Is a value
                if (!comments.isEmpty()) {
                    valuesComments.put(getPath(path, trimSplit[0]), new ArrayList<>(comments));
                    comments.clear();
                }
            }
        }
    }

    private String getPath(ConfigPath path, String key) {
        String stringPath = path.getPath();
        if (stringPath.trim().length() == 0)
            return key;

        else
            return stringPath + "." + key;
    }

    private OPair<String, Integer> trim(String line) {
        StringBuilder builder = new StringBuilder();

        boolean foundChar = false;
        int spaces = 0;

        for (char c : line.toCharArray()) {
            if (c == ' ' && !foundChar) {
                spaces++;

            } else {
                foundChar = true;
                builder.append(c);
            }
        }

        return new OPair<>(builder.toString(), spaces);
    }

    private static class LineIterator {
        private String[] lines;
        private int index = -1;

        @SneakyThrows
        public LineIterator(File file) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            lines = reader.lines().toArray(String[]::new);
        }

        public boolean next() {
            index++;
            return lines.length - 1 >= index;
        }

        public String get() {
            return lines[index];
        }

        public String[] getNext(int amount) {
            List<String> next = new ArrayList<>();
            int cloneIndex = index;
            for (int i = 0; i < amount; i++) {
                cloneIndex++;
                if (lines.length - 1 < cloneIndex) break;
                next.add(lines[cloneIndex]);
            }
            return next.toArray(new String[0]);
        }
    }

    public static class ConfigPath {
        private Map<Integer, String> parents = new HashMap<>();

        public String getPath() {
            List<Integer> spaces = new ArrayList<>(parents.keySet());
            spaces.sort(Integer::compareTo);
            return spaces.stream().map(key -> parents.get(key)).collect(Collectors.joining("."));
        }

        public void setValue(OPair<String, Integer> keyPair) {
            new LinkedHashSet<>(parents.keySet()).stream().filter(integer -> integer >= keyPair.getSecond()).forEach(integer -> parents.remove(integer));
        }

        public void setSection(String sectionName, OPair<String, Integer> line) {
            String section = parents.get(line.getSecond());
            new LinkedHashSet<>(parents.keySet()).stream().filter(integer -> integer >= line.getSecond()).forEach(integer -> parents.remove(integer));

            if (section == null)
                parents.put(line.getSecond(), sectionName);

            else {
                parents.remove(line.getSecond());
                parents.put(line.getSecond(), sectionName);
            }
        }
    }
}
