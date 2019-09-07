package com.oop.orangeengine.main;

import com.oop.orangeengine.main.component.AEngineComponent;
import com.oop.orangeengine.main.util.DefaultInitialization;
import com.oop.orangeengine.main.util.JarUtil;
import sun.tools.jar.resources.jar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClassLoader {

    public static void load() {
        try {

            JarFile jarFile = JarUtil.getJarFile(ClassLoader.class);

            final Enumeration<JarEntry> entries = jarFile.entries();
            List<String> classNames = new ArrayList<>();

            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("com.oop.orangeEngine"))
                    classNames.add(entry.getName());

            }

            jarFile.close();
            for (String className : classNames) {

                Class klass = Class.forName(className);
                if (klass.getName().startsWith("com.oop"))
                    for (Constructor constructor : klass.getConstructors())
                        if(constructor.getDeclaredAnnotation(DefaultInitialization.class) != null) {
                            try {
                                constructor.newInstance();
                            } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignored) {}
                        }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}