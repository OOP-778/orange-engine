package com.oop.orangeengine.yaml.mapper;

public class PrimitveMapper {

    public static boolean is(Object primitive, Class nonPrimitive) {
        return primitive.getClass().getTypeName().equalsIgnoreCase(nonPrimitive.getTypeName());
    }

    public static Object remap(Object object, Class required) {
        if (!required.getSimpleName().toLowerCase().startsWith(object.getClass().getSimpleName().toLowerCase())) return object;

        // Integer
        if (required == Integer.class)
            return Integer.valueOf(object.toString());

        if (required == int.class)
            return Integer.parseInt(object.toString());

        // Double
        if (required == Double.class)
            return Double.valueOf(object.toString());

        if (required == double.class)
            return Double.parseDouble(object.toString());

        // Long
        if (required == Long.class)
            return Long.valueOf(object.toString());

        if (required == long.class)
            return Long.parseLong(object.toString());

        // Double
        if (required == Boolean.class)
            return Boolean.valueOf(object.toString());

        if (required == boolean.class)
            return Boolean.parseBoolean(object.toString());

        return object;
    }

}
