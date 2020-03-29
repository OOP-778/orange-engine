package com.oop.orangeengine.hologram.newV;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter
public class Hologram {

    @Setter
    private Function<Player, List<String>> textRequester;

    @Setter
    private Function<Player, Boolean> viewRequester;

    @Setter
    private double spacing = 0.21;

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

    void update() {}
}
