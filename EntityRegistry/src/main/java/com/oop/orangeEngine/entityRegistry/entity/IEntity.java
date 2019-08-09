package com.oop.orangeEngine.entityRegistry.entity;

import org.bukkit.entity.Entity;

public interface IEntity<E extends Entity> {

    String classPath();

}
