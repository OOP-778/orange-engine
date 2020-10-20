package com.oop.orangeengine.hologram.rules;

import com.oop.orangeengine.hologram.Hologram;
import org.bukkit.entity.Player;

public class RadiusRule implements HologramRule {
    private double radius;
    public RadiusRule(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean canSee(Hologram hologram, Player player) {
        return hologram.getBaseLocation().current().distance(player.getLocation()) <= radius;
    }
}
