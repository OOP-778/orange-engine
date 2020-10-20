package com.oop.orangeengine.hologram.util;

import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.version.OVersion;
import lombok.SneakyThrows;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViewUtil {
    static {
        try {
            Class PLAYER_CHUNK_MAP_CLAZZ = OSimpleReflection.findClass("{nms}.PlayerChunkMap");
            Class WORLD_CLASS = OSimpleReflection.findClass("{nms}.WorldServer");
            Class CRAFT_WORLD_CLASS = OSimpleReflection.findClass("{cb}.CraftWorld");

            WORLD_GET_HANDLE = OSimpleReflection.getMethod(CRAFT_WORLD_CLASS, "getHandle");

            if (OVersion.isOrAfter(14)) {
                Class CHUNK_PAIR_CLASS = OSimpleReflection.findClass("{nms}.ChunkCoordIntPair");
                Class CHUNK_PROVIDER_CLASS = OSimpleReflection.findClass("{nms}.ChunkProviderServer");
                CAN_SEE_14_PLUS = OSimpleReflection.getMethod(PLAYER_CHUNK_MAP_CLAZZ, "isOutsideOfRange", CHUNK_PAIR_CLASS);
                CHUNK_PAIR_CONSTRCUTOR = OSimpleReflection.getConstructor(CHUNK_PAIR_CLASS, int.class, int.class);

                WORLD_GET_CHUNK_PROVIDER = OSimpleReflection.getMethod(WORLD_CLASS, "getChunkProvider");
                PLAYER_CHUNK_MAP = CHUNK_PROVIDER_CLASS.getDeclaredField("playerChunkMap");
            } else {
                CAN_SEE_1_13_MINUS = OSimpleReflection.getMethod(PLAYER_CHUNK_MAP_CLAZZ, "a", int.class, int.class);
                WORLD_GET_PLAYER_CHUNK_MAP = OSimpleReflection.getMethod(WORLD_CLASS, "getPlayerChunkMap");
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize View Utils. Cause of", throwable);
        }
    }

    private static Method
            CAN_SEE_1_13_MINUS,
            CAN_SEE_14_PLUS,
            WORLD_GET_HANDLE,
            WORLD_GET_CHUNK_PROVIDER,
            WORLD_GET_PLAYER_CHUNK_MAP;

    private static Constructor
            CHUNK_PAIR_CONSTRCUTOR;

    private static Field
            PLAYER_CHUNK_MAP;

    @SneakyThrows
    public static boolean canSee(Player player, int x, int z) {
        World world = player.getWorld();
        Object nmsWorld = WORLD_GET_HANDLE.invoke(world);

        if (OVersion.isOrAfter(14)) {
            Object chunkProvider = WORLD_GET_CHUNK_PROVIDER.invoke(nmsWorld);

            PLAYER_CHUNK_MAP.setAccessible(true);
            Object playerChunkMap = PLAYER_CHUNK_MAP.get(chunkProvider);

            return !(boolean) CAN_SEE_14_PLUS.invoke(playerChunkMap, CHUNK_PAIR_CONSTRCUTOR.newInstance(x, z));
        } else {
            Object playerChunkMap = WORLD_GET_PLAYER_CHUNK_MAP.invoke(nmsWorld);
            return (boolean) CAN_SEE_1_13_MINUS.invoke(playerChunkMap, x, z);
        }
    }

}
