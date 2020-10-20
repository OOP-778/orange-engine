package com.oop.testingPlugin;

import com.oop.orangeengine.main.util.OSimpleReflection;

public class Runner {
    public static void main(String[] args) {
        Object a = (long) 10;
        Object s = (short) 10;

        System.out.println(OSimpleReflection.PrimitivesCaster.cast(byte.class, a));
        System.out.println(((Long)a).byteValue());
    }

    public static <T> T cast(Object o, Class<T> type) {
        return (T) o;
    }

}
