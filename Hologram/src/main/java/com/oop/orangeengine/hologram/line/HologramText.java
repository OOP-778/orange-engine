package com.oop.orangeengine.hologram.line;

import com.oop.orangeengine.hologram.Hologram;
import com.oop.orangeengine.hologram.HologramLine;
import com.oop.orangeengine.hologram.wrapped.WrappedArmorStand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import org.bukkit.Location;

public class HologramText extends HologramLine<HologramText> {
    private String text;
    private Hologram hologram;
    private WrappedArmorStand armorStand;

    private OPair<Location, Boolean> location = new OPair<>(null, false);

    public HologramText(String text) {
        this.text = text;
    }

    @Override
    public HologramText _returnThis() {
        return this;
    }
}
