package com.oop.orangeEngine.yaml.mapper;

public class PrimitveMapper {

    public static boolean is(Object primitive, Class nonPrimitive) {
        return primitive.getClass().getTypeName().equalsIgnoreCase(nonPrimitive.getTypeName());
    }

}
