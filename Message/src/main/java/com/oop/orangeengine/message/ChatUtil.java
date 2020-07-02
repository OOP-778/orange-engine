package com.oop.orangeengine.message;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Pattern;

public class ChatUtil {

    public static String makeSureNonNull(Object object) {
        return object == null ? "null" : String.valueOf(object);
    }

    public static String parseHexColor(String hexColor){
        if(hexColor.length() != 6 && hexColor.length() != 3)
            return hexColor;

        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        int multiplier = hexColor.length() == 3 ? 2 : 1;

        System.out.println("Parsing: " + hexColor);

        for(char ch : hexColor.toCharArray()) {
            for(int i = 0; i < multiplier; i++)
                magic.append(ChatColor.COLOR_CHAR).append(ch);
        }

        return magic.toString();
    }
}
