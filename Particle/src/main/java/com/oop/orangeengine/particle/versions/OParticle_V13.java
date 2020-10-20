package com.oop.orangeengine.particle.versions;

import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

public class OParticle_V13 extends OParticle implements NewParticles {
    @Override
    public void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range, List<Player> targetPlayers) {
        if (color != null && (particle.equalsIgnoreCase("SPELL_MOB") || particle.equalsIgnoreCase("SPELL_MOB_AMBIENT"))) {
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
            try {
                data = material.createBlockData();
                if (data == null) {
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (particle.equalsIgnoreCase("REDSTONE")) {
            // color is required for 1.13
            if (color == null) {
                color = Color.RED;
            }
            data = new Particle.DustOptions(color, size);
        }

        newDisplay(particle, center, offsetX, offsetY, offsetZ, speed, amount, data, range, targetPlayers);
    }

    @Override
    public void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Object data, double range, List<Player> targetPlayers) {
        newDisplay(particle, center, offsetX, offsetY, offsetZ, speed, amount, data, range, targetPlayers);
    }
}
