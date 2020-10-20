package com.oop.orangeengine.hologram.wrapped;

import com.oop.orangeengine.hologram.HologramLine;
import com.oop.orangeengine.main.util.version.OVersion;
import org.bukkit.Location;

public class WrappedArmorStand extends WrappedEntity<HologramLine> {
    public WrappedArmorStand(HologramLine owner, Location location) {
        super(owner, location);
        setEntity(ReflectionConstant.createArmorStand(location));

        // Set defaults
        ReflectionConstant.invoke(ReflectionConstant.SET_GRAVITY_METHOD, getEntity(), OVersion.isOrAfter(12));
        if (!OVersion.isBefore(9))
            ReflectionConstant.invoke(ReflectionConstant.SET_MARKER_METHOD, getEntity(), true);
        ReflectionConstant.invoke(ReflectionConstant.SET_SMALL_METHOD, getEntity(), true);
        ReflectionConstant.invoke(ReflectionConstant.SET_CUSTOM_NAME_VISIBLE_METHOD, getEntity(), true);
        ReflectionConstant.invoke(ReflectionConstant.SET_VISIBLE_METHOD, getEntity(), true);
    }

    public void setCustomNameVisible(boolean visible) {
        ReflectionConstant.invoke(ReflectionConstant.SET_CUSTOM_NAME_VISIBLE_METHOD, getEntity(), visible);
    }
}
