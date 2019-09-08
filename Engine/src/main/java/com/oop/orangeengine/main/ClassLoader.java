package com.oop.orangeengine.main;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.JarUtil;

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
        try {

            JarFile jarFile = JarUtil.getJarFile(ClassLoader.class);

            final Enumeration<JarEntry> entries = jarFile.entries();
            List<String> classNames = new ArrayList<>();

            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                if (entry.getName().contains(".class"))
                    classNames.add(entry.getName().replace("/", ".").replace(".class", ""));

            }

            jarFile.close();
            for (String className : classNames) {

                Class klass = Class.forName(className);

                System.out.println("Checking " + klass);
                for (Constructor constructor : klass.getConstructors())
                    if (constructor.getDeclaredAnnotation(DefaultInitialization.class) != null) {
                        try {
                            constructor.newInstance();
                            System.out.println("Initialized " + klass);
                        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignored) {}
                    }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
