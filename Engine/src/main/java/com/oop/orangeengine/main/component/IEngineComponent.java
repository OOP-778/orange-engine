package com.oop.orangeengine.main.component;

public interface IEngineComponent {

    default void onEnable() {
    }

    default void onDisable() {
    }

    String getName();

}
