package com.oop.orangeengine.main.util;

import com.oop.orangeengine.main.Helper;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class OActionBar {
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

            // Packet Constructor
            packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass));
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

    public static void sendActionBar(@NonNull String message, @NonNull Player ...players) {
        Object packet = null;

        try {
            Object component = CHAT_COMPONENT_TEXT.invoke(Helper.color(message));
            packet = PACKET.invoke(component, CHAT_MESSAGE_TYPE);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        for (Player player : players) {
            OSimpleReflection.Player.sendPacket(player, packet);
        }
    }
}
