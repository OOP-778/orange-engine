package com.oop.orangeengine.hologram;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.protocol.HologramProtocol;
import io.netty.channel.Channel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;

import static com.oop.orangeengine.main.Engine.getEngine;

public class HologramController {

    private Map<Location, Hologram> activeHolograms = Maps.newConcurrentMap();

    private static HologramController instance;
    static {
        new HologramController(getEngine().getOwning());
    }

    public static HologramController getInstance() {
        return instance;
    }

    private HologramProtocol protocol;
    private HologramController(JavaPlugin plugin) {
        instance = this;
        protocol = new HologramProtocol(plugin);
    }

    public Hologram getHologram(Location location) {
        return activeHolograms.get(location);
    }
}
