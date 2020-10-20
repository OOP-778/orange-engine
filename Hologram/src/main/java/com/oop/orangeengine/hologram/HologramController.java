package com.oop.orangeengine.hologram;

import com.oop.orangeengine.hologram.protocol.HologramProtocol;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.util.data.pair.OPair;
import io.netty.util.internal.ConcurrentSet;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oop.orangeengine.main.Engine.getEngine;

public class HologramController {
    private Map<String, Map<OPair<Integer, Integer>, Set<Hologram>>> holograms = new ConcurrentHashMap<>();

    private HologramProtocol protocol;

    public HologramController(EnginePlugin plugin, long updateEvery) {
        // Update Task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            holograms.values().forEach(chunksMap -> chunksMap.values().stream().flatMap(Set::stream).forEach(Hologram::update));
        }, updateEvery, updateEvery);

        // Unload holograms at the disable
        plugin.onDisable(() -> holograms.values().forEach(chunksMap -> chunksMap.values().stream().flatMap(Set::stream).forEach(Hologram::remove)));
    }

    public void registerHologram(Hologram hologram) {
        Location baseLocation = hologram.getBaseLocation().current();
        int chunkX = baseLocation.getBlockX() >> 4;
        int chunkZ = baseLocation.getBlockZ() >> 4;

        Map<OPair<Integer, Integer>, Set<Hologram>> worldHolograms =
                holograms.computeIfAbsent(baseLocation.getWorld().getName(), name -> new ConcurrentHashMap<>());

        Set<Hologram> holograms = worldHolograms.computeIfAbsent(new OPair<>(chunkX, chunkZ), pair -> new ConcurrentSet<>());
        holograms.add(hologram);
    }

    public Set<Hologram> getHolograms(int chunkX, int chunkZ, String world, Player player) {
        Map<OPair<Integer, Integer>, Set<Hologram>> worldHolograms = holograms.get(world);
        if (worldHolograms == null) return new HashSet<>();

        Set<Hologram> holograms = worldHolograms.get(new OPair<>(chunkX, chunkZ));
        if (holograms == null) return new HashSet<>();

        return holograms
                .stream()
                .filter(hologram -> player == null || hologram.isViewer(player))
                .collect(Collectors.toSet());
    }
}
