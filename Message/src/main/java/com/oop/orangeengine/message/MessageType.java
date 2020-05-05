package com.oop.orangeengine.message;

public enum MessageType {
    CHAT,
    TITLE,
    ACTION_BAR;

    public static MessageType match(String value) {
        value = value.toLowerCase().replace("_", "");
        for (MessageType type : values()) {
            if (type.name().toLowerCase().replace("_", "").contentEquals(value))
                return type;
        }
        return null;
    }
}
