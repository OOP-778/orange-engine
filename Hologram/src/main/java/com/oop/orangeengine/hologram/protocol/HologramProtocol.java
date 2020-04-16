package com.oop.orangeengine.hologram.protocol;

import com.oop.orangeengine.main.util.OSimpleReflection;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class HologramProtocol extends TinyProtocol {

    private static final Class entityUsePacket = OSimpleReflection.findClass("{nms}.PacketPlayInUseEntity");
    private static final Class vector3DClass = OSimpleReflection.findClass("{nms}.Vec3D");
    private static final Field vector3dField = OSimpleReflection.getField(entityUsePacket, vector3DClass);
    private static final Class entityUseEnumClass = OSimpleReflection.findClass("{nms}.PacketPlayInUseEntity$EnumEntityUseAction");
    private static final Field vector3dXField = OSimpleReflection.getField(vector3DClass, null, double.class, 0);
    private static final Field vector3dZField = OSimpleReflection.getField(vector3DClass, null, double.class, 1);
    private static final Field vector3dYField = OSimpleReflection.getField(vector3DClass, null, double.class, 2);
    private static final Field idField = OSimpleReflection.getField(entityUsePacket, int.class);
    private static final Field actionField = OSimpleReflection.getField(entityUsePacket, true, "action");
    public HologramProtocol(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
        if (packet.getClass().isAssignableFrom(entityUsePacket)) {
            EntityUsePacket entityUsePacket = EntityUsePacket.get(sender, packet);

        }
        return super.onPacketInAsync(sender, channel, packet);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    private static class EntityUsePacket {
        private Location location;
        private int entityId;
        private String action;

        public static EntityUsePacket get(Player player, Object packet) {
            try {
                Location location = new Location(
                        player.getWorld(),
                        (double) vector3dXField.get(packet),
                        (double) vector3dYField.get(packet),
                        (double) vector3dZField.get(packet)
                );
                int entityId = (int) idField.get(packet);
                String action = ((Enum) actionField.get(packet)).name();

                return new EntityUsePacket(location, entityId, action);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to decode packet " + packet.getClass().getSimpleName(), throwable);
            }
        }
    }
}
