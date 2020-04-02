package com.oop.orangeengine.hologram.rules;

import com.oop.orangeengine.hologram.Hologram;
import org.bukkit.entity.Player;

public interface HologramRule {
    boolean canSee(Hologram hologram, Player player);
}
