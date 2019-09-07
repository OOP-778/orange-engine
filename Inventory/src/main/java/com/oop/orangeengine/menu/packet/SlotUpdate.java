package com.oop.orangeengine.menu.packet;

import com.oop.orangeengine.main.util.OSimpleReflection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SlotUpdate {

    //Methods
    private static Method PLAYER_GET_HANDLE_METHOD;
    private static Method PLAYER_SEND_PACKET_METHOD;
    private static Method ITEM_STACK_AS_NMS_COPY;

    //Fields
    private static Field PLAYER_CONNECTION_FIELD;
    private static Field PLAYER_ACTIVE_CONTAINER_FIELD;
    private static Field CONTAINER_WINDOWID_FIELD;

    //Const
    private static Constructor<?> PACKET_SET_SLOT_CONST;

    //init
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

            PLAYER_CONNECTION_FIELD = OSimpleReflection.getField(ENTITY_PLAYER_CLASS, true, "playerConnection");
            PLAYER_ACTIVE_CONTAINER_FIELD = OSimpleReflection.getField(ENTITY_PLAYER_CLASS, false, "activeContainer");

            PLAYER_GET_HANDLE_METHOD = OSimpleReflection.getMethod(CRAFT_PLAYER_CLASS, "getHandle");
            PLAYER_SEND_PACKET_METHOD = OSimpleReflection.getMethod(PLAYER_CONNECTION_CLASS, "sendPacket", PACKET_CLASS);

            PACKET_SET_SLOT_CONST = OSimpleReflection.getConstructor(PACKET_SET_SLOT_CLASS, int.class, int.class, ITEM_STACK_CLASS);
            CONTAINER_WINDOWID_FIELD = OSimpleReflection.getField(CONTAINER_CLASS, true, "windowId");

            ITEM_STACK_AS_NMS_COPY = OSimpleReflection.getMethod(CRAFT_ITEM_CLASS, "asNMSCopy", ItemStack.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void update(Player player, int slot, ItemStack item) {

        try {

            Object ENTITY_PLAYER = PLAYER_GET_HANDLE_METHOD.invoke(player);
            Object ACTIVE_CONTAINER = PLAYER_ACTIVE_CONTAINER_FIELD.get(ENTITY_PLAYER);
            int windowId = CONTAINER_WINDOWID_FIELD.getInt(ACTIVE_CONTAINER);
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
