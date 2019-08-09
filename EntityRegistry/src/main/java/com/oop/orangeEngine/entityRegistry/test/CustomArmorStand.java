package com.oop.orangeEngine.entityRegistry.test;

import com.oop.orangeEngine.entityRegistry.annotation.MethodOverride;
import com.oop.orangeEngine.entityRegistry.entity.AbstractEntity;
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
    public void onJump(double j, double z) {



    }

}
