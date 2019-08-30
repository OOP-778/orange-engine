package com.oop.orangeengine.menu.config;

public enum ButtonType {
    FILLER,
    SWAPPABLE;

    public static ButtonType matchType(String type) {
        for (ButtonType type2 : values())
            if (type2.beautified().equalsIgnoreCase(type))
                return type2;

        return null;
    }

    public String beautified() {
        return name().replace("_", " ").toLowerCase();
    }
}
