package com.oop.orangeengine.hologram.wrapped;


import com.google.common.base.Preconditions;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectionConstant {
    public static Method
            SET_GRAVITY_METHOD,
            SET_VISIBLE_METHOD,
            SET_SMALL_METHOD,
            SET_CUSTOM_NAME_VISIBLE_METHOD,
            SET_CUSTOM_NAME_METHOD,
            SET_MARKER_METHOD,
            SET_LOCATION_METHOD,
            GET_ID,
            WORLD_GET_HANDLE_METHOD,
            AS_NMS_COPY_METHOD,
            GET_DATA_WATCHER,
            GET_LIST,
            ADD_PASSENGER,
            CHAT_COMPONENT_FROM_STRING;

    public static Class<?>
            PACKET_ENTITY_METADATA_CLASS,
            PACKET_LIVING_ENTITY_SPAWN_CLASS,
            PACKET_ENTITY_REMOVE_CLASS,
            PACKET_ENTITY_TELEPORT_CLASS,
            ARMOR_STAND_CLASS,
            CRAFT_WORLD_CLASS,
            NMS_WORLD_CLASS;

    public static Constructor<?>
            PACKET_ENTITY_METADATA_CONST,
            PACKET_LIVING_ENTITY_SPAWN_CONST,
            PACKET_ENTITY_SPAWN_CONST,
            PACKET_ENTITY_REMOVE_CONST,
            PACKET_ENTITY_TELEPORT_CONST,
            ARMOR_STAND_CONST,
            PACKET_ATTACH_CONST,
            ITEM_CONST,
            MOVE_ENTITY_PACKET_CONST;

    public static Field
            ENTITY_META_DATA_ID_FIELD,
            ENTITY_META_DATA_DATA_WATCHER_FIELD;

    static {
        try {
            // Classes
            PACKET_LIVING_ENTITY_SPAWN_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutSpawnEntityLiving");
            PACKET_ENTITY_REMOVE_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityDestroy");
            PACKET_ENTITY_METADATA_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityMetadata");
            PACKET_ENTITY_TELEPORT_CLASS = OSimpleReflection.findClass("{nms}.PacketPlayOutEntityTeleport");
            ARMOR_STAND_CLASS = OSimpleReflection.Package.NMS.getClass("EntityArmorStand");
            CRAFT_WORLD_CLASS = OSimpleReflection.Package.CB.getClass("CraftWorld");
            NMS_WORLD_CLASS = OSimpleReflection.Package.NMS.getClass("World");
            Class<?> CHAT_COMPONENT_TEXT_CLASS = OSimpleReflection.findClass("{nms}.ChatComponentText");
            Class<?> ENTITY_LIVING_CLASS = OSimpleReflection.Package.NMS.getClass("EntityLiving");
            Class<?> ENTITY_CLASS = OSimpleReflection.Package.NMS.getClass("Entity");
            Class<?> ICHAT_COMPONENT_CLASS = OSimpleReflection.findClass("{nms}.IChatBaseComponent");

            // Constructors
            PACKET_LIVING_ENTITY_SPAWN_CONST = OSimpleReflection.getConstructor(PACKET_LIVING_ENTITY_SPAWN_CLASS, ENTITY_LIVING_CLASS);
            PACKET_ENTITY_REMOVE_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_REMOVE_CLASS, int[].class);
            PACKET_ENTITY_METADATA_CONST = PACKET_ENTITY_METADATA_CLASS.getConstructor();
            PACKET_ENTITY_METADATA_CONST.setAccessible(true);

            Class PACKET_ENTITY_SPAWN_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutSpawnEntity");
            PACKET_ENTITY_SPAWN_CONST = OVersion.isBefore(14)
                    ? OSimpleReflection.getConstructor(PACKET_ENTITY_SPAWN_CLASS, ENTITY_CLASS, int.class, int.class)
                    : OSimpleReflection.getConstructor(PACKET_ENTITY_SPAWN_CLASS, ENTITY_CLASS);

            Class ITEM_ENTITY_CLASS = OSimpleReflection.findClass("{nms}.EntityItem");
            Class ITEM_CLASS = OSimpleReflection.findClass("{nms}.ItemStack");
            Class CRAFT_ITEM = OSimpleReflection.findClass("{cb}.inventory.CraftItemStack");

            AS_NMS_COPY_METHOD = OSimpleReflection.getMethod(CRAFT_ITEM, "asNMSCopy", ItemStack.class);
            ITEM_CONST = OSimpleReflection.getConstructor(ITEM_ENTITY_CLASS, NMS_WORLD_CLASS, double.class, double.class, double.class, ITEM_CLASS);

            Class PACKET_ATTACH_CLASS = OSimpleReflection.findClass("{nms}.PacketPlayOutAttachEntity");
            PACKET_ATTACH_CONST = OVersion.isBefore(9)
                    ? OSimpleReflection.getConstructor(PACKET_ATTACH_CLASS, int.class, ENTITY_CLASS, ENTITY_CLASS)
                    : OSimpleReflection.getConstructor(OSimpleReflection.findClass("{nms}.PacketPlayOutMount"), ENTITY_CLASS);

            ARMOR_STAND_CONST = OSimpleReflection.getConstructor(ARMOR_STAND_CLASS, NMS_WORLD_CLASS, double.class, double.class, double.class);
            PACKET_ENTITY_TELEPORT_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_TELEPORT_CLASS, ENTITY_CLASS);

            if (OVersion.isOrAfter(13))
                CHAT_COMPONENT_FROM_STRING = OSimpleReflection.getMethod(OSimpleReflection.findClass("{cb}.util.CraftChatMessage"), "fromStringOrNull");

            // Methods
            SET_GRAVITY_METHOD = OVersion.isBefore(10)
                    ? OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setGravity", boolean.class)
                    : OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setNoGravity", boolean.class);
            SET_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setInvisible", boolean.class);
            SET_SMALL_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setSmall", boolean.class);
            SET_MARKER_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setMarker", boolean.class);
            SET_CUSTOM_NAME_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomNameVisible", boolean.class);
            SET_CUSTOM_NAME_METHOD = OVersion.isOrAfter(13)
                    ? OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomName", ICHAT_COMPONENT_CLASS)
                    : OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomName", String.class);
            SET_LOCATION_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setPosition", double.class, double.class, double.class);
            GET_ID = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getId");
            WORLD_GET_HANDLE_METHOD = OSimpleReflection.getMethod(CRAFT_WORLD_CLASS, "getHandle");

            GET_DATA_WATCHER = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getDataWatcher");
            Class DATA_WATCHER_CLASS = OSimpleReflection.findClass("{nms}.DataWatcher");
            GET_LIST = OSimpleReflection.getMethod(DATA_WATCHER_CLASS, "b");
            ADD_PASSENGER = OVersion.isOrAfter(9)
                    ? OSimpleReflection.getMethod(ENTITY_CLASS, "startRiding", ENTITY_CLASS)
                    : OSimpleReflection.getMethod(ENTITY_CLASS, "mount", ENTITY_CLASS);

            // Fields
            ENTITY_META_DATA_ID_FIELD = OSimpleReflection.getField(PACKET_ENTITY_METADATA_CLASS, "a");
            ENTITY_META_DATA_DATA_WATCHER_FIELD = OSimpleReflection.getField(PACKET_ENTITY_METADATA_CLASS, "b");

            Class ENTITY_MOVE_CLASS = OSimpleReflection.findClass("{nms}.PacketPlayOutEntity$PacketPlayOutRelEntityMove");
            MOVE_ENTITY_PACKET_CONST = OSimpleReflection.getConstructor(ENTITY_MOVE_CLASS, int.class, byte.class, byte.class, byte.class, boolean.class);
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to initialize Reflection.", ex);
        }
    }

    @SneakyThrows
    public static void sendMetaDataUpdate(WrappedEntity<?> entity) {
        Object packet = PACKET_ENTITY_METADATA_CONST.newInstance();
        ENTITY_META_DATA_ID_FIELD.set(packet, entity.getId());
        ENTITY_META_DATA_DATA_WATCHER_FIELD.set(packet, entity.getDataList());

        for (Player viewer : entity.getViewers())
            OSimpleReflection.Player.sendPacket(viewer, packet);
    }

    @SneakyThrows
    public static Object createArmorStand(Location location) {
        Object nmsWorld = invoke(WORLD_GET_HANDLE_METHOD, location.getWorld());
        return ARMOR_STAND_CONST.newInstance(nmsWorld, location.getX(), location.getY(), location.getZ());
    }

    @SneakyThrows
    public static Object createItem(Location location, ItemStack itemStack) {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(itemStack, "ItemStack cannot be null");

        Object nmsWorld = invoke(WORLD_GET_HANDLE_METHOD, location.getWorld());
        return ITEM_CONST.newInstance(nmsWorld, location.getX(), location.getY(), location.getZ(), AS_NMS_COPY_METHOD.invoke(null, itemStack));
    }

    @SneakyThrows
    public static void teleport(WrappedEntity<?> entity) {
        Object packet = PACKET_ENTITY_TELEPORT_CONST.newInstance(entity.getEntity());
        entity.getViewers().forEach(player -> OSimpleReflection.Player.sendPacket(player, packet));
    }

    public static <T> T invoke(Method method, Object owner, Object... args) {
        Preconditions.checkArgument(owner != null, "Invoking object is null");
        Preconditions.checkArgument(method != null, "Method is null");

        try {
            return (T) method.invoke(owner, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @SneakyThrows
    public static void sendMetaDataUpdate(WrappedEntity entity, Player player) {
        Object packet = PACKET_ENTITY_METADATA_CONST.newInstance();
        ENTITY_META_DATA_ID_FIELD.set(packet, entity.getId());
        ENTITY_META_DATA_DATA_WATCHER_FIELD.set(packet, entity.getDataList());

        OSimpleReflection.Player.sendPacket(player, packet);
    }
}