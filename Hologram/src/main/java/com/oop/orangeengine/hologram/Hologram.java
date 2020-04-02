package com.oop.orangeengine.hologram;

import com.oop.orangeengine.hologram.rules.HologramRule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public class Hologram {

    @Setter
    private double spacing = 0.21;

    private Location baseLocation;

    private Set<HologramRule> rules = new HashSet<>();
    private SetWrapper<HologramLine> hologramLines = new SetWrapper<>();

    public Optional<HologramLine> getLine(int index) {
        return hologramLines.get(index);
    }

    public Hologram setLine(int index, HologramLine line) {
        hologramLines.set(index, line);
        update();
        return this;
    }

    public Hologram setLine(int index, String line) {
        update();
        return this;
    }

    public Hologram insertLine(int index, HologramLine line, SetWrapper.InsertionRule rule) {
        hologramLines.insert(index, line, rule);
        update();
        return this;
    }

    public Hologram insertLine(int index, HologramLine line) {
        hologramLines.insert(index, line);
        update();
        return this;
    }

    void update() {
    }
}
