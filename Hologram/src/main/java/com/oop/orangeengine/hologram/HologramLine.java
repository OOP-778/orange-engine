package com.oop.orangeengine.hologram;

import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.rules.HologramRule;
import com.oop.orangeengine.hologram.wrapped.WrappedArmorStand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

@Getter
public abstract class HologramLine<T> implements HologramAttachable {

    private WrappedArmorStand wrappedArmorStand;
    private OPair<Location, Boolean> location = new OPair<>(null, false);
    private Set<Player> viewers = Sets.newConcurrentHashSet();
    private Set<HologramRule> viewRules = Sets.newConcurrentHashSet();
    private Hologram hologram;

    public T setLocation(Location location) {
        this.location.set(location, true);
        return _returnThis();
    }

    public T addRule(HologramRule rule) {
        this.viewRules.add(rule);
        return _returnThis();
    }

    @Override
    public void onAttach(Hologram hologram) {
        this.hologram = hologram;
        this.location = new OPair<>(hologram.getLastLocation().clone().add(0, hologram.getSpacing().getKey(), 0), false);
        this.wrappedArmorStand = new WrappedArmorStand(this, location.getFirst());
    }

    public void update() {}

    public abstract T _returnThis();
}
