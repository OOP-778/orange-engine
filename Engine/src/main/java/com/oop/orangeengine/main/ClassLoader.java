package com.oop.orangeengine.main;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.JarUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassLoader {
    public static void load(java.lang.ClassLoader loader) {
        try {
            String[] split = getEngine().getOwning().getClass().getName().split("\\.");

            int len = split.length == 3 ? 3 : 2;
            int count = 0;
            StringBuilder path = new StringBuilder();

            for (String s : split) {
                if (count == len) break;

                path.append(s).append(".");
                count++;
            }

            String finalPath = path.substring(0, path.length()-1);
            for (ClassPath.ClassInfo info : ClassPath.from(loader).getAllClasses()) {
                if (!info.getName().startsWith(finalPath)) continue;

                Class clazz = null;
                try {
                    clazz = Class.forName(info.getName(), true, loader);
                } catch (Throwable ignored) {}
                if (clazz == null) continue;

                try {
                    for (Constructor<?> declaredConstructor : clazz.getDeclaredConstructors())
                        if (declaredConstructor.getDeclaredAnnotation(DefaultInitialization.class) != null)
                            declaredConstructor.newInstance();
                } catch (Throwable ignored) {}
            }
        } catch (Throwable thrw) {
            thrw.printStackTrace();
        }
    }
}
