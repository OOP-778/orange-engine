package com.oop.orangeengine.entityregistry.entity;

import org.bukkit.entity.Entity;

public interface IEntity<E extends Entity> {

    String classPath();

}
