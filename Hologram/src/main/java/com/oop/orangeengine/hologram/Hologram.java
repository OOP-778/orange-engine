package com.oop.orangeengine.hologram;

import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.line.HologramText;
import com.oop.orangeengine.hologram.rules.HologramRule;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Optional;
import java.util.Set;

@Getter
public class Hologram {
    private OPair<Float, Boolean> spacing = new OPair<>(0.21f, false);
    private Location baseLocation;

    @Getter(value = AccessLevel.PACKAGE)
    private Location lastLocation;

    @Getter(AccessLevel.PACKAGE)
    private Set<HologramRule> rules = Sets.newConcurrentHashSet();

    private SetWrapper<HologramLine> hologramLines = new SetWrapper<>();

    @Getter(AccessLevel.PRIVATE)
    private Set<HologramLine> newLines = Sets.newConcurrentHashSet();

    public Optional<HologramLine> getLine(int index) {
        return hologramLines.get(index);
    }

    public Hologram setLine(int index, HologramLine line) {
        hologramLines.set(index, line);
        update();
        return this;
    }

    public Hologram setLine(int index, String text) {
        hologramLines.set(index, new HologramText(text));
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

    private void updateNew() {
        for (HologramLine line : newLines) {

        }
    }

    void update() {
    }
}
