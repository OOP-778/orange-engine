package com.oop.orangeengine.message.impl.chat.color;

import com.oop.orangeengine.main.util.version.OVersion;

import java.util.function.Function;

public class OChatColor {
    private static Function<String, ChatColor> provider;

    static {
        if (OVersion.isOrAfter(16))
            provider = ChatColor_16::new;
        else
            provider = ChatColor_8::new;
    }

    public static ChatColor match(String color) {
        return provider.apply(color);
    }
}
