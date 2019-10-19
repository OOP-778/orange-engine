package com.oop.orangeengine.main;

import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.JarUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassLoader {

    public static void load() {
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
                        try {
                            constructor.newInstance();
                        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex) {
                        }
                    }
            } catch (Throwable ex) {
                if (!(ex instanceof NoClassDefFoundError))
                    ex.printStackTrace();
            }
        }
    }

}
