package com.oop.orangeengine.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Helper {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void print(Object object) {
        Bukkit.getConsoleSender().sendMessage(color(object.toString()));
    }

    public static <T> T[] toArray(T... args) {
        return args;
    }


    public static <T> T[] toArray(Collection<T> c, T[] a) {
        return c.size()>a.length ?
                c.toArray((T[])Array.newInstance(a.getClass().getComponentType(), c.size())) :
                c.toArray(a);
    }

    /** The collection CAN be empty */
    public static <T> T[] toArray(Collection<T> c, Class klass) {
        return toArray(c, (T[])Array.newInstance(klass, c.size()));
    }

    /** The collection CANNOT be empty! */
    public static <T> T[] toArray(Collection<T> c) {
        return toArray(c, c.iterator().next().getClass());
    }

    public static Collection<OfflinePlayer> getOfflinePlayers() {
        return Collections.unmodifiableCollection(Arrays.asList(Bukkit.getOfflinePlayers()));
    }

    public static Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(Bukkit.getOnlinePlayers());
    }
}
