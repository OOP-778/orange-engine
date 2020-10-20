package com.oop.orangeengine.message.impl.chat.color;

public class ChatColor_8 implements ChatColor {

    private Object colorObject;

    public ChatColor_8(String color) {
        if (color.length() == 1)
            colorObject = net.md_5.bungee.api.ChatColor.getByChar(color.toCharArray()[0]);
        else
            colorObject = net.md_5.bungee.api.ChatColor.valueOf(color.toUpperCase());
    }

    @Override
    public Object getColorObject() {
        return colorObject;
    }

    @Override
    public String getName() {
        return ((net.md_5.bungee.api.ChatColor)colorObject).name();
    }
}
