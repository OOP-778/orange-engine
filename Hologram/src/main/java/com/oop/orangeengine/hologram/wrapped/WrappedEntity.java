package com.oop.orangeengine.hologram.wrapped;

import com.oop.orangeengine.hologram.HologramLine;
import com.oop.orangeengine.hologram.HologramPlaceholders;
import com.oop.orangeengine.hologram.util.UpdateableObject;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;

@Getter
public abstract class WrappedEntity<T extends HologramLine> {
    private Object entity;
    private int id;

    private UpdateableObject<Location> location = new UpdateableObject<>(null);
    private UpdateableObject<String> customName = new UpdateableObject<>("");
    private T owner;

    public WrappedEntity(T owner, Location location) {
        this.owner = owner;
        this.location.set(location);
    }

    protected void setEntity(Object entity) {
        this.entity = entity;
        id = ReflectionConstant.invoke(ReflectionConstant.GET_ID, entity);
    }

    @SneakyThrows
    public List getDataList() {
        return (List) ReflectionConstant.GET_LIST.invoke(ReflectionConstant.GET_DATA_WATCHER.invoke(getEntity()));
    }

    private Map<UUID, String> oldNames = new HashMap<>();

    @SneakyThrows
    public void update() {
        if (customName.isUpdated()) {
            for (Player viewer : getViewers()) {
                String s = customName.get();
                for (BiFunction<Player, String, String> registeredPlaceholder : HologramPlaceholders.getRegisteredPlaceholders())
                    s = registeredPlaceholder.apply(viewer, s);

                String oldName = oldNames.get(viewer.getUniqueId());
                if (oldName != null && oldName.contentEquals(s)) continue;

                oldNames.remove(viewer.getUniqueId());
                oldNames.put(viewer.getUniqueId(), s);

                ReflectionConstant.invoke(
                        ReflectionConstant.SET_CUSTOM_NAME_METHOD,
                        entity,
                        OVersion.isOrAfter(13)
                                ? ReflectionConstant.CHAT_COMPONENT_FROM_STRING.invoke(null, s)
                                : s
                );
            }
        }

        if (location.isUpdated()) {
            ReflectionConstant.teleport(this);
        }

        for (Player viewer : getViewers()) {
            updateMeta(viewer);
        }
    }

    public void setLocation(Location location) {
        if (location.equals(this.location.get())) return;
        this.location.set(location);
        ReflectionConstant.invoke(ReflectionConstant.SET_LOCATION_METHOD, getEntity(), location.getX(), location.getY(), location.getZ());
    }

    Location getLocation() {
        return location.get();
    }

    public String getCustomName() {
        return customName.get();
    }

    public void setCustomName(String customName) {
        this.customName.set(Helper.color(customName));
    }

    public Set<Player> getViewers() {
        return owner.getHologram().getViewers();
    }

    public void remove() {
        getViewers().forEach(this::remove);
    }

    @SneakyThrows
    public void addPassenger(Object entity, Player player) {
        try {
            ReflectionConstant.ADD_PASSENGER.invoke(entity, getEntity());
            attach(entity, player);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to add a passenger to an entity", throwable);
        }
    }

    private void attach(Object entity, Player player) {
        try {
            Object packet = OVersion.isBefore(9)
                    ? ReflectionConstant.PACKET_ATTACH_CONST.newInstance(0, entity, getEntity())
                    : ReflectionConstant.PACKET_ATTACH_CONST.newInstance(getEntity());
            OSimpleReflection.Player.sendPacket(player, packet);

        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to attach entity to the armorstand", throwable);
        }
    }

    public void remove(Player player) {
        try {
            Object removePacket = ReflectionConstant.PACKET_ENTITY_REMOVE_CONST.newInstance(new int[]{getId()});
            OSimpleReflection.Player.sendPacket(player, removePacket);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to remove entity for " + player.getName(), e);
        }
    }

    public void spawn(Player player) {
        try {
            System.out.println("Spawning for " + player.getName());
            Object spawnPacket;
            if (this instanceof WrappedArmorStand)
                spawnPacket = ReflectionConstant.PACKET_LIVING_ENTITY_SPAWN_CONST.newInstance(getEntity());
            else
                spawnPacket = OVersion.isBefore(14)
                        ? ReflectionConstant.PACKET_ENTITY_SPAWN_CONST.newInstance(getEntity(), 2, 1)
                        : ReflectionConstant.PACKET_ENTITY_SPAWN_CONST.newInstance(getEntity());
            OSimpleReflection.Player.sendPacket(player, spawnPacket);
            updateMeta(player);

        } catch (Throwable e) {
            throw new IllegalStateException("Failed to spawn armor stand for " + player.getName(), e);
        }
    }

    public void updateMeta() {
        ReflectionConstant.sendMetaDataUpdate(this);
    }

    public void updateMeta(Player player) {
        System.out.println("Meta UPdate");
        ReflectionConstant.sendMetaDataUpdate(this, player);
    }

    @SneakyThrows
    public void move(Location newLocation, Location lastLocation) {
        if (OVersion.isOrAfter(14)) return;
        double deltaX = newLocation.getX() * 32 - lastLocation.getX() * 32;
        double deltaY = newLocation.getY() * 32 - lastLocation.getY() * 32;
        double deltaZ = newLocation.getZ() * 32 - lastLocation.getZ() * 32;

        System.out.println("Moving");

        Object packet = OSimpleReflection.initializeObject(
                ReflectionConstant.MOVE_ENTITY_PACKET_CONST,
                id,
                deltaX,
                deltaY,
                deltaZ,
                false
        );
        System.out.println("Sent packet");

        getViewers().forEach(player -> OSimpleReflection.Player.sendPacket(player, packet));
    }
}
