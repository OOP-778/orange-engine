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
            for (ClassPath.ClassInfo info : ClassPath.from(loader).getAllClasses()) {
                Class clazz = null;
                try {
                    clazz = Class.forName(info.getName(), false, loader);
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
