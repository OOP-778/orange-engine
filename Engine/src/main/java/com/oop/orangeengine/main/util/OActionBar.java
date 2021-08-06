package com.oop.orangeengine.main.util;

import com.oop.orangeengine.main.Helper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public interface OActionBar {

    OActionBar PROVIDER = getProvider();

    static OActionBar getProvider() {
        try {
            Class.forName("io.papermc.paper.adventure.PaperAdventure");
            return new Paper();
        } catch (Throwable throwable) {
            return new Legacy();
        }
    }

    void sendActionBar(@NonNull String message, @NonNull Player... players);

    class Paper implements OActionBar {

        private final Field PLAIN_ADVENTURE_HANDLER;
        private final Method deserializeMethod, sendMethod;

        public Paper() {
            try {
                Class<?> PAPER_ADVENTURE_CLASS, PLAIN_COMPONENT_SERIALIZER_CLASS, AUDIENCE_CLASS, COMPONENT_CLASS;
                PLAIN_COMPONENT_SERIALIZER_CLASS = Class.forName("net.kyori.adventure.text.serializer.plain.PlainComponentSerializer");
                PAPER_ADVENTURE_CLASS = Class.forName("io.papermc.paper.adventure.PaperAdventure");
                AUDIENCE_CLASS = Class.forName("net.kyori.adventure.audience.Audience");
                COMPONENT_CLASS = Class.forName("net.kyori.adventure.text.Component");

                PLAIN_ADVENTURE_HANDLER = PAPER_ADVENTURE_CLASS.getField("PLAIN");

                deserializeMethod = PLAIN_COMPONENT_SERIALIZER_CLASS.getDeclaredMethod("deserialize", String.class);
                sendMethod = AUDIENCE_CLASS.getDeclaredMethod("sendActionBar", COMPONENT_CLASS);

            } catch (Throwable throwable) {
                throw new IllegalStateException("Error while initializing reflections for action bar", throwable);
            }
        }

        @Override
        @SneakyThrows
        public void sendActionBar(@NonNull String message, @NonNull Player... players) {
            final Object componentSerializer = PLAIN_ADVENTURE_HANDLER.get(null);
            final Object component = deserializeMethod.invoke(componentSerializer, message);

            for (Player player : players) {
                sendMethod.invoke(player, component);
            }
        }
    }

    class Legacy implements OActionBar {
        /**
         * ChatComponentText JSON message builder.
         */
        private static final MethodHandle CHAT_COMPONENT_TEXT;
        /**
         * PacketPlayOutChat
         */
        private static final MethodHandle PACKET;
        /**
         * GAME_INFO enum constant.
         */
        private static final Object CHAT_MESSAGE_TYPE;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> packetPlayOutChatClass = OSimpleReflection.findClass("{nms}.PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = OSimpleReflection.findClass("{nms}.IChatBaseComponent");

            MethodHandle packet = null;
            MethodHandle chatComp = null;
            Object chatMsgType = null;

            try {
                // Game Info Message Type
                Class<?> chatMessageTypeClass = OSimpleReflection.findClass("{nms}.ChatMessageType");
                for (Object obj : chatMessageTypeClass.getEnumConstants()) {
                    if (obj.toString().equals("GAME_INFO")) {
                        chatMsgType = obj;
                        break;
                    }
                }

                // JSON Message Builder
                Class<?> chatComponentTextClass = OSimpleReflection.findClass("{nms}.ChatComponentText");
                chatComp = lookup.findConstructor(chatComponentTextClass, MethodType.methodType(void.class, String.class));

                try {
                    // Packet Constructor
                    packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass));
                } catch (Exception ex) {
                    packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass, UUID.class));
                }
            } catch (Throwable ignored) {
                try {
                    // Game Info Message Type
                    chatMsgType = (byte) 2;

                    // JSON Message Builder
                    Class<?> chatComponentTextClass = OSimpleReflection.findClass("{nms}.ChatComponentText");
                    chatComp = lookup.findConstructor(chatComponentTextClass, MethodType.methodType(void.class, String.class));

                    // Packet Constructor
                    packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, byte.class));
                } catch (NoSuchMethodException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }

            CHAT_MESSAGE_TYPE = chatMsgType;
            CHAT_COMPONENT_TEXT = chatComp;
            PACKET = packet;
        }

        public void sendActionBar(@NonNull String message, @NonNull Player... players) {
            Object packet = null;

            try {
                Object component = CHAT_COMPONENT_TEXT.invoke(Helper.color(message));
                try {
                    packet = PACKET.invoke(component, CHAT_MESSAGE_TYPE);
                } catch (Throwable throwable) {
                    packet = PACKET.invoke(component, CHAT_MESSAGE_TYPE, new UUID(0L, 0L));
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            for (Player player : players) {
                OSimpleReflection.Player.sendPacket(player, packet);
            }
        }
    }
}
