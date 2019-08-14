package com.oop.orangeengine.yaml.util;

public class UnreadString {

    private int index;
    private String value;

    public UnreadString(int index, String value) {
        this.index = index;
        this.value = value;
    }

    public int index() {
        return index;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        if (value.trim().length() == 0) return "";
        return value;
    }
}
