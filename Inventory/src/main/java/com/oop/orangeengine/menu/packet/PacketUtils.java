package com.oop.orangeengine.menu.packet;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.version.OVersion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static com.oop.orangeengine.main.Engine.getEngine;
import static com.oop.orangeengine.main.Helper.assertTrue;

public class PacketUtils {

    // Methods
    private static Method PLAYER_GET_HANDLE_METHOD, ITEM_STACK_AS_NMS_COPY, PLAYER_SEND_PACKET_METHOD, PLAYER_UPDATE_INVENTORY_METHOD;

    // Fields
    private static Field PLAYER_CONNECTION_FIELD, CONTAINER_WINDOWID_FIELD, PLAYER_DEFAULT_CONTAINER_FIELD, PLAYER_ACTIVE_CONTAINER_FIELD;

    // Const
    private static Constructor<?> PACKET_SET_SLOT_CONST, CHAT_MESSAGE_CONST, PACKET_OPEN_WINDOW_CONST;

    private static Map<Integer, Object> containers13 = Maps.newHashMap();

    // init
    static {
        try {

            Class<?> PACKET_SET_SLOT_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutSetSlot");
            Class<?> ENTITY_PLAYER_CLASS = OSimpleReflection.Package.NMS.getClass("EntityPlayer");
            Class<?> CRAFT_PLAYER_CLASS = OSimpleReflection.Package.CB_ENTITY.getClass("CraftPlayer");
            Class<?> PLAYER_CONNECTION_CLASS = OSimpleReflection.Package.NMS.getClass("PlayerConnection");
            Class<?> PACKET_CLASS = OSimpleReflection.Package.NMS.getClass("Packet");
            Class<?> ITEM_STACK_CLASS = OSimpleReflection.Package.NMS.getClass("ItemStack");
            Class<?> CONTAINER_CLASS = OSimpleReflection.Package.NMS.getClass("Container");
            Class<?> CRAFT_ITEM_CLASS = OSimpleReflection.Package.CB_INVENTORY.getClass("CraftItemStack");
            Class<?> CHAT_MESSAGE_CLASS = OSimpleReflection.Package.NMS.getClass("ChatMessage");
            Class<?> PACKET_OPEN_WINDOW_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutOpenWindow");
            Class<?> ICHAT_BASE_COMPONENT_CLASS = OSimpleReflection.Package.NMS.getClass("IChatBaseComponent");

            PLAYER_CONNECTION_FIELD = OSimpleReflection.getField(ENTITY_PLAYER_CLASS, "playerConnection");
            PLAYER_ACTIVE_CONTAINER_FIELD = OSimpleReflection.getField(ENTITY_PLAYER_CLASS, "activeContainer");

            PLAYER_GET_HANDLE_METHOD = OSimpleReflection.getMethod(CRAFT_PLAYER_CLASS, "getHandle");
            PLAYER_SEND_PACKET_METHOD = OSimpleReflection.getMethod(PLAYER_CONNECTION_CLASS, "sendPacket", PACKET_CLASS);

            PACKET_SET_SLOT_CONST = OSimpleReflection.getConstructor(PACKET_SET_SLOT_CLASS, int.class, int.class, ITEM_STACK_CLASS);
            CONTAINER_WINDOWID_FIELD = OSimpleReflection.getField(CONTAINER_CLASS, "windowId");
            PLAYER_DEFAULT_CONTAINER_FIELD = OSimpleReflection.getField(ENTITY_PLAYER_CLASS, "defaultContainer");

            ITEM_STACK_AS_NMS_COPY = OSimpleReflection.getMethod(CRAFT_ITEM_CLASS, "asNMSCopy", ItemStack.class);

            CHAT_MESSAGE_CONST = CHAT_MESSAGE_CLASS.getConstructor(String.class, Object[].class);

            if (OVersion.isAfter(13)) {
                Class<?> CONTAINERS_CLASS = OSimpleReflection.Package.NMS.getClass("Containers");
                PACKET_OPEN_WINDOW_CONST = PACKET_OPEN_WINDOW_CLASS.getConstructor(int.class, CONTAINERS_CLASS, ICHAT_BASE_COMPONENT_CLASS);

                for (int i = 1; i != 7; i++)
                    containers13.put(i * 9, CONTAINERS_CLASS.getField("GENERIC_9X" + i).get(null));

            } else
                PACKET_OPEN_WINDOW_CONST = PACKET_OPEN_WINDOW_CLASS.getConstructor(int.class, String.class, ICHAT_BASE_COMPONENT_CLASS, int.class);

            PLAYER_UPDATE_INVENTORY_METHOD = ENTITY_PLAYER_CLASS.getMethod("updateInventory", CONTAINER_CLASS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updateTitle(Player player, Inventory inventory, String title) {
        title = Helper.color(title);

        if (inventory.getTitle().contentEquals(title)) return;

        getEngine().getLogger().printDebug("Changing title for inventory " + inventory.getHolder());
        getEngine().getLogger().printDebug("Old title: " + inventory.getTitle() + ", lenght: " + inventory.getTitle().length());
        getEngine().getLogger().printDebug("New title: " + title + ", lenght: " + title.length());
        assertTrue(title.toCharArray().length < 32, "Failed to update title for " + player.getName() + " because title is too long! Max characters can be 32!");

        try {
            final Object chatMessageTitle = CHAT_MESSAGE_CONST.newInstance(title, new Object[0]);
            final Object entityPlayer = PLAYER_GET_HANDLE_METHOD.invoke(player);
            final Object activeContainer = PLAYER_ACTIVE_CONTAINER_FIELD.get(entityPlayer);
            final int windowId = CONTAINER_WINDOWID_FIELD.getInt(activeContainer);

            Object packet;
            if (OVersion.isAfter(13)) {
                final Object container = containers13.get(inventory.getSize());
                packet = PACKET_OPEN_WINDOW_CONST.newInstance(windowId, container, chatMessageTitle);

            } else
                packet = PACKET_OPEN_WINDOW_CONST.newInstance(windowId, "minecraft:chest", chatMessageTitle, inventory.getSize());

            sendPacket(player, packet);
            PLAYER_UPDATE_INVENTORY_METHOD.invoke(entityPlayer, activeContainer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updateSlot(Player player, int slot, ItemStack item, boolean top) {
        try {

            Object entityPlayer = PLAYER_GET_HANDLE_METHOD.invoke(player);
            Object container;
            if (top)
                container = PLAYER_ACTIVE_CONTAINER_FIELD.get(entityPlayer);

            else
                container = PLAYER_DEFAULT_CONTAINER_FIELD.get(entityPlayer);

            int windowId = CONTAINER_WINDOWID_FIELD.getInt(container);
            Object itemStack = ITEM_STACK_AS_NMS_COPY.invoke(item, item);

            sendPacket(PLAYER_GET_HANDLE_METHOD.invoke(player), PACKET_SET_SLOT_CONST.newInstance(windowId, slot, itemStack));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void sendPacket(Object player, Object packet) {
        try {

            Object CONNECTION = PLAYER_CONNECTION_FIELD.get(player);
            PLAYER_SEND_PACKET_METHOD.invoke(CONNECTION, packet);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
