package com.oop.orangeengine.particle.versions;

import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class NonLegacyParticle extends OParticle {
    @Override
    public void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range, List<Player> targetPlayers) {

    }

    @Override
    protected void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Object data, double range, List<Player> targetPlayers) {

    }
}
