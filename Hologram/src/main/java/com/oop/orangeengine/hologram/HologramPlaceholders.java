package com.oop.orangeengine.hologram;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HologramPlaceholders {

    @Getter
    private static Set<BiFunction<Player, String, String>> registeredPlaceholders = new HashSet<>();

    public static void registerGlobalPlaceholder(Function<String, String> function) {
        registeredPlaceholders.add((player, s) -> function.apply(s));
    }

    public static void registerPlayerPlaceholder(BiFunction<Player, String, String> function) {
        registeredPlaceholders.add(function);
    }
}
