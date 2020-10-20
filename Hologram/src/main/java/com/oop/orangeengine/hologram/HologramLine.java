package com.oop.orangeengine.hologram;

import com.google.common.collect.Sets;
import com.oop.orangeengine.hologram.rules.HologramRule;
import com.oop.orangeengine.hologram.util.UpdateableObject;
import com.oop.orangeengine.hologram.wrapped.WrappedArmorStand;
import com.oop.orangeengine.main.util.Updateable;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@Getter
public abstract class HologramLine<T>  {
    protected WrappedArmorStand wrappedArmorStand;
    protected UpdateableObject<Location> location = new UpdateableObject<>(null);
    protected Hologram hologram;

    private Set<UUID> spawnedFor = Sets.newConcurrentHashSet();

    public T setLocation(Location location) {
        if (this.location.get() != null && this.location.get().equals(location)) return (T) this;
        this.location.set(location);
        return (T) this;
    }

    protected void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public void preUpdate() {
        if (wrappedArmorStand == null)
            wrappedArmorStand = new WrappedArmorStand(this, location.get());
    }

    public void update() {}

    private Location lastLocation;

    public void postUpdate() {
        // Spawn for new viewers
        for (Player viewer : hologram.getViewers()) {
            if (!spawnedFor.contains(viewer.getUniqueId())) {
                spawnedFor.add(viewer.getUniqueId());
                handleAdd(viewer);
            }
        }

        // Remove for those who aren't inside the list anymore
        for (UUID uuid : Sets.newHashSet(spawnedFor)) {
            if (hologram.getViewers().stream().noneMatch(player -> player.getUniqueId() == uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    handleRemove(player);
                spawnedFor.remove(uuid);
            }
        }

        // Update location
        if (location.isUpdated()) {
            wrappedArmorStand.setLocation(location.get());
            if (lastLocation != null && lastLocation.distance(location.get()) <= 8) {
                wrappedArmorStand.move(location.get(), lastLocation);
            }
            lastLocation = location.get();
        }

        wrappedArmorStand.update();
    }

    public void remove() {
        wrappedArmorStand.remove();
    }

    protected void handleRemove(Player player) {
        wrappedArmorStand.remove(player);
    }

    protected void handleAdd(Player player) {
        wrappedArmorStand.spawn(player);
    }
}
