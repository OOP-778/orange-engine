package com.oop.orangeEngine.particle;

import com.oop.orangeEngine.main.util.version.OVersion;
import com.oop.orangeEngine.particle.versions.OParticle_V12;
import com.oop.orangeEngine.particle.versions.OParticle_V13;
import com.oop.orangeEngine.particle.versions.OParticle_V8;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public abstract class OParticle {

    public static OParticle getProvider() {
        OParticle display;

        if (OVersion.isAfter(12))
            display = new OParticle_V13();
        else if (OVersion.isAfter(8))
            display = new OParticle_V12();
        else
            display = new OParticle_V8();

        return display;
    }

    public void display(String particle, Location center, int amount) {
        display(particle, center, 0, 0, 0, 0f, amount, 1, null, null, (byte) 1, 0, Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()));
    }

    public abstract void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range, List<Player> targetPlayers);

    protected abstract void display(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Object data, double range, List<Player> targetPlayers);

    protected void displayItem(String particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Material material, byte materialData, double range, List<Player> targetPlayers) {
        if (material == null || material == Material.AIR) {
            return;
        }

        ItemStack item = new ItemStack(material);
        item.setDurability(materialData);
        display(particle, center, offsetX, offsetY, offsetZ, speed, amount, item, range, targetPlayers);
    }

    protected void displayLegacyColored(String particle, Location center, float speed, Color color, double range, List<Player> targetPlayers) {
        int amount = 0;
        // Colored particles can't have a speed of 0.
        if (speed == 0) {
            speed = 1;
        }
        float offsetX = (float) color.getRed() / 255;
        float offsetY = (float) color.getGreen() / 255;
        float offsetZ = (float) color.getBlue() / 255;

        // The redstone particle reverts to red if R is 0!
        if (offsetX < Float.MIN_NORMAL) {
            offsetX = Float.MIN_NORMAL;
        }

        display(particle, center, offsetX, offsetY, offsetZ, speed, amount, null, range, targetPlayers);
    }
}