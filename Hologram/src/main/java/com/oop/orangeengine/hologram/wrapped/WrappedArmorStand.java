package com.oop.orangeengine.hologram.wrapped;

import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.HologramLine;
import com.oop.orangeengine.hologram.wrapped.impl.WrappedDataWatcher;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static com.oop.orangeengine.hologram.wrapped.WrappedArmorStand.ReflectionConstant.*;

public class WrappedArmorStand {
    private Object entityArmorStand;
    private OPair<Location, Boolean> location;
    private OPair<String, Boolean> customName = new OPair<>("", false);

    private WrappedDataWatcher dataWatcher;

    @Getter
    private int id;

    private OPair<Boolean, Boolean> marker = new OPair<>(true, false);
    private OPair<Boolean, Boolean> small = new OPair<>(true, false);

    public HologramLine owner;

    private final Set<Player> viewers = Sets.newConcurrentHashSet();

    public WrappedArmorStand(HologramLine owner, Location location) {
        this.owner = owner;
        this.entityArmorStand = createArmorStand(location);
        this.location = new OPair<>(location, false);
        this.id = invoke(GET_ID, entityArmorStand);
        this.dataWatcher = WrappedDataWatcher.construct();

        // Set defaults
        invoke(SET_GRAVITY_METHOD, entityArmorStand, false);
        invoke(SET_MARKER_METHOD, entityArmorStand, true);
        invoke(SET_SMALL_METHOD, entityArmorStand, true);
        invoke(SET_CUSTOM_NAME_VISIBLE_METHOD, entityArmorStand, true);
        invoke(SET_VISIBLE_METHOD, entityArmorStand, false);
    }

    public void update() {
        if (customName.getSecond()) {
            invoke(SET_CUSTOM_NAME_METHOD, customName.getFirst());
            ReflectionConstant.sendMetaDataUpdate(this, getCustomName());
        }

        if (location.getSecond()) {
            invoke(SET_LOCATION_METHOD, entityArmorStand, location.getFirst());
            ReflectionConstant.teleport(this);
        }
    }

    public void remove() {
        viewers.forEach(this::remove);
    }

    public void remove(Player player) {
        try {
            Object id = invoke(GET_ID, entityArmorStand);
            Object removePacket = PACKET_ENTITY_REMOVE_CONST.newInstance(id);

            OSimpleReflection.Player.sendPacket(player, removePacket);
            viewers.remove(player);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to remove armor stand for " + player.getName(), e);
        }
    }

    public void spawn(Player player) {
        try {

            Object spawnPacket = PACKET_ENTITY_SPAWN_CONST.newInstance(entityArmorStand);
            OSimpleReflection.Player.sendPacket(player, spawnPacket);
            viewers.add(player);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to spawn armor stand for " + player.getName(), e);
        }
    }

    static class ReflectionConstant {
        static Method
                SET_GRAVITY_METHOD,
                SET_VISIBLE_METHOD,
                SET_SMALL_METHOD,
                SET_CUSTOM_NAME_VISIBLE_METHOD,
                SET_CUSTOM_NAME_METHOD,
                SET_MARKER_METHOD,
                SET_LOCATION_METHOD,
                GET_ID,
                WORLD_GET_HANDLE_METHOD;

        static Class<?>
                PACKET_ENTITY_METADATA_CLASS,
                PACKET_ENTITY_SPAWN_CLASS,
                PACKET_ENTITY_REMOVE_CLASS,
                PACKET_ENTITY_TELEPORT_CLASS,
                ARMOR_STAND_CLASS,
                CRAFT_WORLD_CLASS,
                NMS_WORLD_CLASS;

        static Constructor<?>
                PACKET_ENTITY_METADATA_CONST,
                PACKET_ENTITY_SPAWN_CONST,
                PACKET_ENTITY_REMOVE_CONST,
                PACKET_ENTITY_TELEPORT_CONST,
                ARMOR_STAND_CONST,
                CHAT_COMPONENT_CONST;

        static Field
                ENTITY_META_DATA_ID_FIELD,
                ENTITY_META_DATA_DATA_WATCHER_FIELD;

        static {
            try {

                // Classes
                PACKET_ENTITY_SPAWN_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutSpawnEntityLiving");
                PACKET_ENTITY_REMOVE_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityDestroy");
                PACKET_ENTITY_METADATA_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityMetadata");
                PACKET_ENTITY_TELEPORT_CLASS = OSimpleReflection.findClass("{nms}.PacketPlayOutEntityTeleport");
                ARMOR_STAND_CLASS = OSimpleReflection.Package.NMS.getClass("EntityArmorStand");
                CRAFT_WORLD_CLASS = OSimpleReflection.Package.CB.getClass("CraftWorld");
                NMS_WORLD_CLASS = OSimpleReflection.Package.NMS.getClass("World");
                Class<?> CHAT_COMPONENT_TEXT_CLASS = OSimpleReflection.findClass("{nms}.ChatComponentText");
                Class<?> ENTITY_LIVING_CLASS = OSimpleReflection.Package.NMS.getClass("EntityLiving");
                Class<?> ENTITY_CLASS = OSimpleReflection.Package.NMS.getClass("Entity");

                // Constructors
                PACKET_ENTITY_SPAWN_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_SPAWN_CLASS, ENTITY_LIVING_CLASS);
                PACKET_ENTITY_REMOVE_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_REMOVE_CLASS, int[].class);
                PACKET_ENTITY_METADATA_CONST = PACKET_ENTITY_METADATA_CLASS.getConstructor();
                PACKET_ENTITY_METADATA_CONST.setAccessible(true);

                ARMOR_STAND_CONST = OSimpleReflection.getConstructor(ARMOR_STAND_CLASS, NMS_WORLD_CLASS, double.class, double.class, double.class);
                PACKET_ENTITY_TELEPORT_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_TELEPORT_CLASS, ENTITY_CLASS);
                CHAT_COMPONENT_CONST = OSimpleReflection.getConstructor(CHAT_COMPONENT_TEXT_CLASS, String.class);

                // Methods
                SET_GRAVITY_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setGravity", boolean.class);
                SET_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setVisible", boolean.class);
                SET_SMALL_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setSmall", boolean.class);
                SET_MARKER_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setMarker", boolean.class);
                SET_CUSTOM_NAME_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomNameVisible", boolean.class);
                SET_CUSTOM_NAME_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomName", String.class);
                SET_LOCATION_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getLocation", Location.class);
                GET_ID = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getId");
                WORLD_GET_HANDLE_METHOD = OSimpleReflection.getMethod(CRAFT_WORLD_CLASS, "getHandle");

                // Fields
                ENTITY_META_DATA_ID_FIELD = PACKET_ENTITY_METADATA_CLASS.getDeclaredField("a");
                ENTITY_META_DATA_DATA_WATCHER_FIELD = PACKET_ENTITY_METADATA_CLASS.getDeclaredField("b");

            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        @SneakyThrows
        static void sendMetaDataUpdate(WrappedArmorStand armorStand, Object object) {
            Object packet = PACKET_ENTITY_METADATA_CONST.newInstance();
            ENTITY_META_DATA_ID_FIELD.set(packet, armorStand.id);

            if (object instanceof String) {
                WrappedDataWatcher dataWatcher = armorStand.dataWatcher;
                ENTITY_META_DATA_DATA_WATCHER_FIELD.set(packet, armorStand.dataWatcher.getList());

                if (OVersion.is(8))
                    dataWatcher.addItem((byte) 4, object);

                else
                    dataWatcher.addItem((byte) 2, OVersion.isAfter(9) ? CHAT_COMPONENT_CONST.newInstance(object.toString()) : object.toString());

                ENTITY_META_DATA_DATA_WATCHER_FIELD.set(packet, dataWatcher);
                armorStand.viewers.forEach(player -> OSimpleReflection.Player.sendPacket(player, packet));
            }
        }

        static Object createArmorStand(Location location) {
            Object nmsWorld = invoke(WORLD_GET_HANDLE_METHOD, location.getWorld());
            try {
                return ARMOR_STAND_CONST.newInstance(nmsWorld, location.getX(), location.getY(), location.getZ());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @SneakyThrows
        public static void teleport(WrappedArmorStand armorStand) {
            Object packet = PACKET_ENTITY_TELEPORT_CONST.newInstance(armorStand.entityArmorStand);
            armorStand.viewers.forEach(player -> OSimpleReflection.Player.sendPacket(player, packet));
        }

        static <T> T invoke(Method method, Object owner, Object... args) {
            try {
                return (T) method.invoke(owner, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    boolean isSmall() {
        return small.getFirst();
    }

    void setSmall(boolean small) { this.small.set(small, true); }

    boolean isMarker() {
        return marker.getFirst();
    }

    void setMarker(boolean marker) {
        this.marker.set(marker, true);
    }

    void setLocation(Location location) {
        this.location.set(location, true);
    }

    Location getLocation() {
        return location.getFirst();
    }

    public String getCustomName() {
        return customName.getFirst();
    }

    public void setCustomName(String customName) {
        this.customName.set(Helper.color(customName), true);
    }
}
