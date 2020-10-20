package com.oop.orangeengine.message.impl.chat.color;

import lombok.SneakyThrows;

public interface ChatColor {

    @SneakyThrows
    default boolean isFormat() {
        return org.bukkit.ChatColor.valueOf(getName().toUpperCase()).isFormat();
    }

    Object getColorObject();

    String getName();
}
