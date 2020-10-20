package com.oop.orangeengine.message.impl.chat.color;

import com.oop.orangeengine.main.util.OSimpleReflection;
import lombok.SneakyThrows;

import java.util.regex.Pattern;

public class ChatColor_16 implements ChatColor {

    private Object colorObject;

    @SneakyThrows
    public ChatColor_16(String color) {
        if (color.length() == 1)
            colorObject = OSimpleReflection
                    .getMethod(net.md_5.bungee.api.ChatColor.class, "getByChar", char.class)
                    .invoke(null, color.toCharArray()[0]);
        else
            colorObject = OSimpleReflection
                    .getMethod(net.md_5.bungee.api.ChatColor.class, "of", String.class)
                    .invoke(null, color);
    }

    @Override
    public Object getColorObject() {
        return colorObject;
    }

    @Override
    @SneakyThrows
    public String getName() {
        return (String) OSimpleReflection
                .getMethod(net.md_5.bungee.api.ChatColor.class, "getName")
                .invoke(getColorObject());
    }
}
