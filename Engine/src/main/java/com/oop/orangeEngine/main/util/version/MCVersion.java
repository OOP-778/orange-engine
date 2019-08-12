package com.oop.orangeEngine.reflection.version;

public enum MCVersion {
    V8(8),
    V9(9),
    V10(10),
    V11(11),
    V12(12),
    V13(13),
    V14(14),
    UNKNOWN(-1);

    private int intVersion;
    MCVersion(int intVersion) {
        this.intVersion = intVersion;
    }
    public int getIntVersion() {
        return intVersion;
    }

    public static MCVersion fromInt(int intVersion) {
        for(MCVersion version : values()) {
            if(version.getIntVersion() == intVersion)
                return version;
        }

        return UNKNOWN;

    }
}
