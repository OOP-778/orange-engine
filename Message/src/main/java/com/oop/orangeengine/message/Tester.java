package com.oop.orangeengine.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {

    private static final Pattern HEX_PATTERN = Pattern.compile("#(?:[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    public static void main(String[] args) {
        String input = "#F08080%player_name% ";

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer builder = new StringBuffer(input);
        while (matcher.find()) {
            System.out.println("Found");
            String group = matcher.group();
            System.out.println("group: " + group);

            input = input.replace(group, "Hello");
        }

        System.out.println(input);
    }

}
