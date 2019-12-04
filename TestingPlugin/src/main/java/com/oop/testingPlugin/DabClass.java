package com.oop.testingPlugin;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;

@RequiredArgsConstructor
public class DabClass implements Serializable {

    private final ItemStack itemStack;
    public UUID uuid = UUID.randomUUID();

    public ChildA childA = new ChildA();

}
