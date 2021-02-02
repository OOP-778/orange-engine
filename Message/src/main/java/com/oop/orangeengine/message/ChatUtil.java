package com.oop.orangeengine.message;

import net.md_5.bungee.api.ChatColor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtil {
    public static String makeSureNonNull(Object object) {
        return object == null ? "null" : String.valueOf(object);
    }

    public static String parseHexColor(String hexColor){
        if(hexColor.length() != 6 && hexColor.length() != 3)
            return hexColor;

        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        int multiplier = hexColor.length() == 3 ? 2 : 1;

        for(char ch : hexColor.toCharArray()) {
            for(int i = 0; i < multiplier; i++)
                magic.append(ChatColor.COLOR_CHAR).append(ch);
        }

        return magic.toString();
    }

    public static <T> String listToString(Collection<T> list) {
        return "[" + list.stream()
                .map(ob -> ob == null ? "null" : ob.toString())
                .collect(Collectors.joining(", ")) + "]";
    }
}
