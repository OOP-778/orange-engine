package com.oop.orangeengine.particle.versions;

import com.oop.orangeengine.main.util.data.cache.OCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface NewParticles {
    OCache<String, Particle> particleCache =
            OCache
                    .builder()
                    .concurrencyLevel(1)
                    .expireAfter(5, TimeUnit.SECONDS)
                    .resetExpireAfterAccess(true)
                    .build();

    default void newDisplay(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Object data, double range, List<Player> targetPlayers) {
        try {
            particle = particle.replaceAll("\\s+", "_").toUpperCase();

            Particle parsedParticle = particleCache.get(particle);
            if (parsedParticle == null) {
                parsedParticle = Particle.valueOf(particle);
                particleCache.put(particle, parsedParticle);
            }

            if (targetPlayers == null) {
                String worldName = center.getWorld().getName();
                double squared = range * range;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().getName().equals(worldName) || range != 0 && player.getLocation().distanceSquared(center) > squared) {
                        continue;
                    }
                    player.spawnParticle(parsedParticle, center, amount, offsetX, offsetY, offsetZ, speed, data);
                }
            } else {
                for (Player player : targetPlayers) {
                    player.spawnParticle(parsedParticle, center, amount, offsetX, offsetY, offsetZ, speed, data);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
