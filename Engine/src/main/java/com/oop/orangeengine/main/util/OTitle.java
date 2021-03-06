package com.oop.orangeengine.main.util;

import com.oop.orangeengine.main.Helper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class OTitle {
    private static final Object TIMES;
    private static final Object TITLE;
    private static final Object SUBTITLE;
    private static final Object CLEAR;

    private static final MethodHandle PACKET;
    private static final MethodHandle CHAT_COMPONENT_TEXT;

    static {
        Class<?> chatComponentText = OSimpleReflection.findClass("{nms}.ChatComponentText");
        Class<?> packet = OSimpleReflection.findClass("{nms}.PacketPlayOutTitle");
        Class<?> titleTypes = packet.getDeclaredClasses()[0];
        MethodHandle packetCtor = null;
        MethodHandle chatComp = null;

        Object times = null;
        Object title = null;
        Object subtitle = null;
        Object clear = null;

        for (Object type : titleTypes.getEnumConstants()) {
            switch (type.toString()) {
                case "TIMES":
                    times = type;
                    break;
                case "TITLE":
                    title = type;
                    break;
                case "SUBTITLE":
                    subtitle = type;
                    break;
                case "CLEAR":
                    clear = type;
            }
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            chatComp = lookup.findConstructor(chatComponentText, MethodType.methodType(void.class, String.class));

            packetCtor = lookup.findConstructor(packet,
                    MethodType.methodType(void.class, titleTypes,
                            OSimpleReflection.findClass("{nms}.IChatBaseComponent"), int.class, int.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        TITLE = title;
        SUBTITLE = subtitle;
        TIMES = times;
        CLEAR = clear;

        PACKET = packetCtor;
        CHAT_COMPONENT_TEXT = chatComp;
    }

    @SneakyThrows
    public static void sendTitle(
            int fadeIn, int stay, int fadeOut,
            String title,
            String subtitle,
            @Nonnull Player ...players
    ) {
        if (title == null && subtitle == null) return;

        Object[] packets = new Object[3];

        Object timesPacket = PACKET.invoke(TIMES, CHAT_COMPONENT_TEXT.invoke(Helper.color(title)), fadeIn, stay, fadeOut);
        packets[0] = timesPacket;

        if (title != null) {
            Object titlePacket = PACKET.invoke(TITLE, CHAT_COMPONENT_TEXT.invoke(Helper.color(title)), fadeIn, stay, fadeOut);
            packets[1] = titlePacket;
        }
        if (subtitle != null) {
            Object subtitlePacket = PACKET.invoke(SUBTITLE, CHAT_COMPONENT_TEXT.invoke(Helper.color(subtitle)), fadeIn, stay, fadeOut);
            packets[2] = subtitlePacket;
        }

        for (Object packet : packets) {
            if (packet == null) continue;

            for (Player player : players) {
                OSimpleReflection.Player.sendPacket(player, packet);
            }
        }
    }

    /**
     * Sends a title message with title and subtitle with normal
     * fade in, stay and fade out time to a player.
     *
     * @param player   the player to send the title to.
     * @param title    the title message.
     * @param subtitle the subtitle message.
     * @since 1.0.0
     */
    public static void sendTitle(@NonNull Player player, String title, String subtitle) {
        sendTitle(10, 20, 10, title, subtitle, player);
    }

    /**
     * Parses and sends a title from the config.
     * The configuration section must at least
     * contain {@code title} or {@code subtitle}
     *
     * <p>
     * <b>Example:</b>
     * <blockquote><pre>
     *     ConfigurationSection titleSection = plugin.getConfig().getConfigurationSection("restart-title");
     *     Titles.sendTitle(player, titleSection);
     * </pre></blockquote>
     *
     * @param player the player to send the title to.
     * @param config the configuration section to parse the title properties from.
     * @since 1.0.0
     */

    /**
     * Clears the title and subtitle message from the player's screen.
     *
     * @param player the player to clear the title from.
     * @since 1.0.0
     */
    public static void clearTitle(@NonNull Player player) {
        Object clearPacket = null;

        try {
            clearPacket = PACKET.invoke(CLEAR, null, -1, -1, -1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        OSimpleReflection.Player.sendPacket(player, clearPacket);
    }
}
