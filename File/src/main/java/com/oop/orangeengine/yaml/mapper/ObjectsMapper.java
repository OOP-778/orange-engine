package com.oop.orangeengine.yaml.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ObjectsMapper {

    private static List<Function<String, Object>> stringToObjectMappers = new ArrayList<>();
    private static List<Function<Object, String>> objectToStringMappers = new ArrayList<>();

    static {

        stringToObjectMappers.add((string) -> {

            if (isDouble(string)) {
                return Double.parseDouble(string);
            } else return null;

        });

        stringToObjectMappers.add((string) -> {

            if (isInteger(string)) {
                return Integer.parseInt(string);
            } else return null;
        });

        stringToObjectMappers.add((string) -> {

            if (isBoolean(string)) {
                return Boolean.valueOf(string);
            } else return null;

        });

        objectToStringMappers.add((object) -> {

            if (PrimitveMapper.is(object, Boolean.class)) return ((Boolean) object).toString();
            else return null;

        });

        objectToStringMappers.add((object) -> {

            if (PrimitveMapper.is(object, Integer.class)) return object.toString();
            else return null;

        });

        objectToStringMappers.add((object) -> {

            if (PrimitveMapper.is(object, Double.class)) return object.toString();
            else return null;

        });


    }

    public static Object mapObject(String string) {


        final Object[] object = {null};
        stringToObjectMappers.forEach(func -> {

            if (object[0] != null) return;
            object[0] = func.apply(string);

        });

        return object[0] == null ? string : object[0];

    }

    public static String toString(Object value) {

        final String[] serialized = {null};
        objectToStringMappers.forEach(func -> {

            if (serialized[0] != null) return;
            serialized[0] = func.apply(value);

        });

        String v = serialized[0] != null ? serialized[0] : value.toString();
        if (value instanceof String) {
            v = "\"" + v + "\"";
        }

        return v;

    }

    private static boolean isDouble(String string) {

        if (string.contains(".")) {

            try {
                Double.valueOf(string);
                return true;
            } catch (Exception ex) {
                return false;
            }

        } else return false;

    }

    private static boolean isInteger(String string) {

        try {
            Integer.valueOf(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isBoolean(String string) {
        return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false");
    }

}
