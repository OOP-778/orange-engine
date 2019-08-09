package com.oop.orangeEngine.main.component;

public interface IEngineComponent {

    default void onEnable() {
    }

    default void onDisable() {
    }

    String getName();

}
