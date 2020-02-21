package com.oop.orangeengine.main;

import com.google.common.collect.Sets;
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
            for (Class<?> clazz : getClasses(loader)) {
                for (Constructor<?> declaredConstructor : clazz.getDeclaredConstructors())
                    if (declaredConstructor.getDeclaredAnnotation(DefaultInitialization.class) != null)
                        declaredConstructor.newInstance();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void load2() {
        JarFile jarFile = JarUtil.getJarFile(getEngine().getOwning().getClass());

        final Enumeration<JarEntry> entries = jarFile.entries();
        List<String> classNames = new ArrayList<>();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().contains(".class")) {
                classNames.add(entry.getName().replace("/", ".").replace(".class", ""));
            }
        }

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String className : classNames) {

            try {
                Class klass = Class.forName(className);
                for (Constructor constructor : klass.getConstructors())
                    if (constructor.getDeclaredAnnotation(DefaultInitialization.class) != null) {
                        constructor.newInstance();
                    }
            } catch (Throwable ex) {
            }
        }
    }

    private static Set<Class> getClasses(java.lang.ClassLoader loader) {
        try {
            Field f = java.lang.ClassLoader.class.getDeclaredField("classes");
            f.setAccessible(true);

            Vector<Class> classes = (Vector<Class>) f.get(loader);
            return new HashSet<>(classes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Sets.newHashSet();
    }

}
