package com.oop.orangeengine.entityregistry.entity;


import lombok.Getter;
import org.bukkit.entity.Entity;

@Getter
public abstract class AbstractEntity<E extends Entity> implements IEntity {

    private E entity;

    public void superExecute(String methodName, Object... args) {

    }

}
