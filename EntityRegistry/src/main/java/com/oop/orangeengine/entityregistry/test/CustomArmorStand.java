package com.oop.orangeengine.entityregistry.test;

import com.oop.orangeengine.entityregistry.annotation.MethodOverride;
import com.oop.orangeengine.entityregistry.entity.AbstractEntity;
import org.bukkit.entity.ArmorStand;

public class CustomArmorStand extends AbstractEntity<ArmorStand> {

    @Override
    public String classPath() {
        return "EntityArmorStand";
    }

    /*
    Example of an NMS method
    When runned it will try to find the right method name as in nms and will override them
    */
    @MethodOverride(names = {"a", "f", "h"})
    public void onJump(double j, double z) {}

}
