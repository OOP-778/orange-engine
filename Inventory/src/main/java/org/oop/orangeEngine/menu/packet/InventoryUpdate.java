package org.oop.orangeEngine.menu.packet;

import com.oop.orangeEngine.reflection.OClass;
import com.oop.orangeEngine.reflection.OReflection;

public class SlotUpdatePacket {

    private static OReflection reflection = OReflection.getInstance();
    private static OClass PACKET_SET_SLOT_CLASS = reflection.resolveNmsClass("PacketPlayOutSetSlot");
    private static OClass ITEM_STACK_CLASS = reflection.resolveNmsClass("ItemStack");
    private static OClass CONTAINER_CLASS = reflection.resolveNmsClass("Container");
    private static OClass CRAFT_ITEM_CLASS = reflection.resolveBukkitClass("inventory.CraftItemStack");

    static {

        PACKET_SET_SLOT_CLASS.getConstructor(int.class, int.class, ITEM_STACK_CLASS.getJavaClass());

        CRAFT_ITEM_CLASS.getMethod("asNMSCopy");
        CONTAINER_CLASS.getField("windowId");

    }

}
