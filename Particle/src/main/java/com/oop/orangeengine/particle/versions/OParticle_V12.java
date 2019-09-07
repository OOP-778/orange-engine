package com.oop.orangeengine.particle.versions;

import com.oop.orangeengine.particle.OParticle;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.List;


public class OParticle_V12 extends OParticle {
    @Override
    public void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range, List<Player> targetPlayers) {
        if (color != null && (particle.equalsIgnoreCase("REDSTONE") || particle.equalsIgnoreCase("SPELL_MOB") || particle.equalsIgnoreCase("SPELL_MOB_AMBIENT"))) {
            displayLegacyColored(particle, center, speed, color, range, targetPlayers);
            return;
        }

        if (particle.equalsIgnoreCase("ITEM_CRACK")) {
            displayItem(particle, center, offsetX, offsetY, offsetZ, speed, amount, material, materialData, range, targetPlayers);
            return;
        }

        Object data = null;
        if (particle.equalsIgnoreCase("BLOCK_CRACK") || particle.equalsIgnoreCase("BLOCK_DUST") || particle.equalsIgnoreCase("FALLING_DUST")) {
            if (material == null || material == Material.AIR) {
                return;
            }
            data = new MaterialData(material, materialData);
        }

        display(particle, center, offsetX, offsetY, offsetZ, speed, amount, data, range, targetPlayers);
    }

    @Override
    protected void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Object data, double range, List<Player> targetPlayers) {
        try {
            if (targetPlayers == null) {
                String worldName = center.getWorld().getName();
                double squared = range * range;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().getName().equals(worldName) || range != 0 && player.getLocation().distanceSquared(center) > squared) {
                        continue;
                    }
                    player.spawnParticle(Particle.valueOf(particle), center, amount, offsetX, offsetY, offsetZ, speed, data);
                }
            } else {

                for (Player player : targetPlayers) {
                    player.spawnParticle(Particle.valueOf(particle), center, amount, offsetX, offsetY, offsetZ, speed, data);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}