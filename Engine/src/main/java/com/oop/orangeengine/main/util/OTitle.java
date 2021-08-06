package com.oop.orangeengine.main.util;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public interface OTitle {

    OTitle PROVIDER = getProvider();

    static OTitle getProvider() {
        return OVersion.isOrAfter(16) ? new Paper() : new Legacy();
    }

    void sendTitle(
            int fadeIn, int stay, int fadeOut,
            String title,
            String subtitle,
            @Nonnull Player... players
    );

    class Paper implements OTitle {

        private final Method sendTitle;

        public Paper() {
            try {
                Class<?> PLAYER_CLASS;
                PLAYER_CLASS = Class.forName("org.bukkit.entity.Player");

                sendTitle = PLAYER_CLASS.getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Error while initializing reflections for title", throwable);
            }
        }

        @Override
        @SneakyThrows
        public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String subtitle, @Nonnull Player... players) {
            for (Player player : players) {
                sendTitle.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            }
        }
    }

    class Legacy implements OTitle {
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
        public void sendTitle(
                int fadeIn, int stay, int fadeOut,
                String title,
                String subtitle,
                @Nonnull Player... players
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
    }
}
