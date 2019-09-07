package com.orangeengine.hologram;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.orangeengine.hologram.WrappedArmorStand.ReflectionConstant.*;

public class WrappedArmorStand {

    private Object entityArmorStand;
    private OPair<Location, Boolean> location;
    private OPair<String, Boolean> customName = new OPair<>("", false);
    @Getter
    private int id;

    private OPair<Boolean, Boolean> marker = new OPair<>(true, false);
    private OPair<Boolean, Boolean> small = new OPair<>(true, false);

    public HoloLine owner;

    public WrappedArmorStand(HoloLine owner, Location location) {
        this.entityArmorStand = createArmorStand(location);
        this.location = new OPair<>(location, false);
        this.id = invoke(GET_ID, entityArmorStand);

        //Set defaults
        invoke(SET_GRAVITY_METHOD, entityArmorStand, false);
        invoke(SET_MARKER_METHOD, entityArmorStand, true);
        invoke(SET_SMALL_METHOD, entityArmorStand, true);
        invoke(SET_CUSTOM_NAME_VISIBLE_METHOD, entityArmorStand, true);
        invoke(SET_VISIBLE_METHOD, entityArmorStand, false);
    }

    public void update() {
        if (customName.getSecond())
            invoke(SET_CUSTOM_NAME_METHOD, entityArmorStand, customName.getFirst());

        if (marker.getSecond())
            invoke(SET_MARKER_METHOD, entityArmorStand, marker.getFirst());

    }

    public void spawn(Player player) {
        try {

            Object spawnPacket = PACKET_ENTITY_SPAWN_CONST.newInstance(entityArmorStand);
            OSimpleReflection.Player.sendPacket(player, spawnPacket);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
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
                GET_DATA_WATCHER,
                GET_ID,
                WORLD_GET_HANDLE_METHOD;

        static Class<?>
                PACKET_ENTITY_METADATA_CLASS,
                PACKET_ENTITY_SPAWN_CLASS,
                PACKET_ENTITY_REMOVE_CLASS,
                ARMOR_STAND_CLASS,
                CRAFT_WORLD_CLASS,
                NMS_WORLD_CLASS;

        static Constructor<?>
                PACKET_ENTITY_METADATA_CONST,
                PACKET_ENTITY_SPAWN_CONST,
                PACKET_ENTITY_REMOVE_CONST,
                ARMOR_STAND_CONST;

        static {
            try {

                // Classes
                PACKET_ENTITY_SPAWN_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutSpawnEntityLiving");
                PACKET_ENTITY_REMOVE_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityDestroy");
                PACKET_ENTITY_METADATA_CLASS = OSimpleReflection.Package.NMS.getClass("PacketPlayOutEntityMetadata");
                ARMOR_STAND_CLASS = OSimpleReflection.Package.NMS.getClass("EntityArmorStand");
                CRAFT_WORLD_CLASS = OSimpleReflection.Package.CB.getClass("CraftWorld");
                NMS_WORLD_CLASS = OSimpleReflection.Package.NMS.getClass("World");
                Class<?> ENTITY_LIVING_CLASS = OSimpleReflection.Package.NMS.getClass("EntityLiving");
                Class<?> DATA_WATCHER_CLASS = OSimpleReflection.Package.NMS.getClass("DataWatcher");

                // Constructors
                PACKET_ENTITY_SPAWN_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_SPAWN_CLASS, ENTITY_LIVING_CLASS);
                PACKET_ENTITY_REMOVE_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_REMOVE_CLASS, int[].class);
                PACKET_ENTITY_METADATA_CONST = OSimpleReflection.getConstructor(PACKET_ENTITY_METADATA_CLASS, int.class, DATA_WATCHER_CLASS, boolean.class);

                ARMOR_STAND_CONST = OSimpleReflection.getConstructor(ARMOR_STAND_CLASS, NMS_WORLD_CLASS, double.class, double.class, double.class);

                // Methods
                SET_GRAVITY_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setGravity", boolean.class);
                SET_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setVisible", boolean.class);
                SET_SMALL_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setSmall", boolean.class);
                SET_MARKER_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setMarker", boolean.class);
                SET_CUSTOM_NAME_VISIBLE_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomNameVisible", boolean.class);
                SET_CUSTOM_NAME_METHOD = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "setCustomName", String.class);
                GET_DATA_WATCHER = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getDataWatcher");
                GET_ID = OSimpleReflection.getMethod(ARMOR_STAND_CLASS, "getId");
                WORLD_GET_HANDLE_METHOD = OSimpleReflection.getMethod(CRAFT_WORLD_CLASS, "getHandle");

            } catch (Exception ex) {
                throw new IllegalStateException(ex);
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

    void setSmall(boolean small) {
        this.small.set(small, true);
    }

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
