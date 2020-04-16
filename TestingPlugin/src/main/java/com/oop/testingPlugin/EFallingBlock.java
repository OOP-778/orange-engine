package com.oop.testingPlugin;

import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFallingBlock;

public class EFallingBlock extends EntityFallingBlock {
    public EFallingBlock(World world, Location location, IBlockData data) {
        super(world, location.getX(), location.getY(), location.getZ(), data);
    }

    @Override
    public void B_(){}
}
