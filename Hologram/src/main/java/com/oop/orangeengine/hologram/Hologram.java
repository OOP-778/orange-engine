package com.oop.orangeengine.hologram;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.line.HologramText;
import com.oop.orangeengine.hologram.rules.HologramRule;
import com.oop.orangeengine.hologram.util.UpdateableObject;
import com.oop.orangeengine.hologram.util.ViewUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Getter
public class Hologram {
    private OPair<Float, Boolean> spacing = new OPair<>(0.24f, false);

    @Getter
    private UpdateableObject<Location> baseLocation = new UpdateableObject<>(null);

    @Getter(value = AccessLevel.PACKAGE)
    private Location lastLocation;

    @Getter(AccessLevel.PACKAGE)
    private Set<HologramRule> rules = Sets.newConcurrentHashSet();

    private CopyOnWriteArrayList<HologramLine> lines = new CopyOnWriteArrayList<>();
    private Set<Player> viewers = Sets.newConcurrentHashSet();

    @Getter(AccessLevel.PRIVATE)
    private Set<HologramLine> newLines = Sets.newConcurrentHashSet();

    public Hologram(Location baseLocation) {
        this.baseLocation.set(baseLocation);
    }

    public Optional<HologramLine> getLine(int index) {
        Preconditions.checkArgument(lines.size() > index, "ArrayIndexOutBounds " + index + "/" + (lines.size() - 1));
        return Optional.ofNullable(lines.get(index));
    }

    public Hologram addLine(String text) {
        return addLine(() -> text);
    }

    public Hologram addLine(HologramLine line) {
        lines.add(line);
        return this;
    }

    public Hologram addLine(Supplier<String> textSupplier) {
        lines.add(new HologramText(textSupplier));
        return this;
    }

    public Hologram setLine(int index, HologramLine line) {
        Preconditions.checkArgument(lines.size() > index, "ArrayIndexOutBounds " + index + "/" + (lines.size() - 1));
        lines.set(index, line);
        return this;
    }

    public Hologram setLine(int index, String text) {
        return setLine(index, new HologramText(text));
    }

    public Hologram insertLine(int index, HologramLine line, InsertMethod method) {
        Preconditions.checkArgument(lines.size() > index, "ArrayIndexOutBounds " + index + "/" + (lines.size() - 1));
        switch (method) {
            case BEFORE:
                lines.add(index, line);
                break;
            case AFTER:
                lines.add(index + 1, line);
                break;
            case REPLACE:
                lines.set(index, line);
                break;
        }
        return this;
    }

    public Hologram insertLine(int index, HologramLine line) {
        return insertLine(index, line, InsertMethod.BEFORE);
    }

    public boolean isViewer(Player player) {
        return viewers.contains(player);
    }

    private Map<HologramLine, Integer> oldIndexes = new HashMap<>();

    void update() {
        // Update the placement of lines
        for (HologramLine line : lines) {
            Integer i = oldIndexes.get(line);
            int i2 = lines.indexOf(line);
            if (i == null || i2 != i || baseLocation.isUpdated()) {
                readjustLocations();
                break;
            }
        }

        // Update the viewers
        viewers.clear();
        for (Player player : Collections.synchronizedCollection(baseLocation.current().getWorld().getPlayers())) {
            if (ViewUtil.canSee(player, baseLocation.current().getBlockX() >> 4, baseLocation.current().getBlockZ() >> 4))
                viewers.add(player);
        }

        viewers.removeIf(viewer -> rules.stream().anyMatch(rule -> !rule.canSee(this, viewer)));

        // Update the lines
        for (HologramLine line : lines) {
            if (line.getHologram() == null)
                line.setHologram(this);

            line.preUpdate();
            line.update();
            line.postUpdate();
        }
    }

    public void readjustLocations() {
        oldIndexes.clear();
        lastLocation = baseLocation.get().clone();
        for (int i = 0; i < lines.size(); i++) {
            HologramLine hologramLine = lines.get(i);
            hologramLine.setLocation(lastLocation);
            lastLocation = lastLocation.clone().add(0, -spacing.getKey(), 0);

            oldIndexes.put(hologramLine, i);
        }
    }

    public void remove() {
        for (HologramLine line : lines) line.remove();
    }
}
