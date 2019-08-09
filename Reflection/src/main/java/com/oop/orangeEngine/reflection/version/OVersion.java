package com.oop.orangeEngine.reflection.version;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class OVersion {

    private static String stringVersion;
    private static int intVersion;
    private static MCVersion mcVersion;

    static {

        String fullVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        stringVersion = StringUtils.replace(fullVersion, "_", " ");
        intVersion = Integer.parseInt(StringUtils.replace(fullVersion.split("_")[1].split("_R")[0], "v", ""));
        mcVersion = MCVersion.fromInt(intVersion);

    }

    public static boolean is(int version2) {
        return intVersion == version2;
    }

    public static boolean isAfter(int version2) {
        return intVersion > version2;
    }

    public static boolean isBefore(int version2) {
        return intVersion < version2;
    }

    public static boolean isOrAfter(int version2) {
        return intVersion == version2 || intVersion > version2;
    }

    public static String getStringVersion() {
        return stringVersion;
    }

    public static int getIntVersion() {
        return intVersion;
    }

    public MCVersion getMcVersion() {
        return mcVersion;
    }
}
