package com.oop.orangeengine.message;


import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;

public class ColorFinder {

    public String color;
    public String decoration;

    private ColorFinder(String color, String decoration) {
        this.color = color;
        this.decoration = decoration;
    }

    public static ColorFinder find(String input) {
        char[] charArray = input.trim().toCharArray();
        String color = "";
        String decoration = "";

        for (Character c : charArray) {
            if (string(c).equalsIgnoreCase("&")) {

                int index = ArrayUtils.indexOf(charArray, c);
                if (arrayHas(index + 1, charArray)) {
                    String secondChar = string(charArray[index + 1]);

                    ChatColor chatColor = findColor(secondChar);
                    if (chatColor == null) continue;

                    if (chatColor.isColor())
                        color = "&" + secondChar;

                    else
                        decoration = "&" + secondChar;

                }

                charArray[index] = '@';
            }
        }

        return new ColorFinder(color, decoration);
    }

    static String string(Object obj) {
        return obj.toString();
    }

    static boolean arrayHas(int index, char[] array) {
        return (array.length - 1) >= index;
    }

    public String color() {
        return color;
    }

    public String decoration() {
        return decoration;
    }

    private static ChatColor findColor(String chaz) {
        for(ChatColor color : ChatColor.values())
            if(color.getChar() == chaz.toLowerCase().toCharArray()[0])
                return color;

        return null;
    }
}
