package com.oop.orangeengine.main;

import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

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

    public static <T> T[] toArray(Collection<T> c, Class klass) {
        return toArray(c, (T[])Array.newInstance(klass, c.size()));
    }

    public static <T> T[] toArray(Collection<T> c) {
        return toArray(c, c.iterator().next().getClass());
    }

    public static Collection<OfflinePlayer> getOfflinePlayers() {
        return Collections.unmodifiableCollection(Arrays.asList(Bukkit.getOfflinePlayers()));
    }

    public static Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(Bukkit.getOnlinePlayers());
    }

    public static OptionalConsumer<Player> getOnlinePlayer(Predicate<Player> filter) {
        return OptionalConsumer.of(getOnlinePlayers().stream().filter(filter).findFirst());
    }

    public static OptionalConsumer<OfflinePlayer> getOfflinePlayer(Predicate<OfflinePlayer> filter) {
        return OptionalConsumer.of(getOfflinePlayers().stream().filter(filter).findFirst());
    }

}