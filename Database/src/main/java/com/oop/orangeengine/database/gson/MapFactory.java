package com.oop.orangeengine.database.gson;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.util.NumberConversions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static com.oop.orangeengine.main.Engine.getEngine;

public class MapFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!Map.class.isAssignableFrom(rawType))
            return null;

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T obj) throws IOException {
                Map<Object, Object> map = (Map<Object, Object>) obj;
                JsonObject mapObject = new JsonObject();

                // Set class of Map
                mapObject.addProperty("class", map.getClass().getName());

                // Init values array for map
                JsonArray array = new JsonArray();
                mapObject.add("values", array);

                map.forEach((key, value) -> {
                    JsonObject pairObject = new JsonObject();
                    if (isNumber(key))
                        pairObject.add("key", numberToObject(key));
                    else
                        pairObject.add("key", gson.toJsonTree(key));

                    if (isNumber(value))
                        pairObject.add("value", numberToObject(value));
                    else
                        pairObject.add("value", gson.toJsonTree(value));

                    array.add(pairObject);
                });

                Streams.write(mapObject, out);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement element = Streams.parse(in);
                if (!element.isJsonObject())
                    throw new JsonParseException("Failed to parse a map because it's not an json object!");

                JsonObject jsonMap = element.getAsJsonObject();
                Class<? extends Map> mapClass;
                Map<Object, Object> map;
                try {
                    mapClass = (Class<? extends Map>) Class.forName(jsonMap.getAsJsonPrimitive("class").getAsString());
                } catch (Throwable thrw) {
                    throw new JsonParseException("Failed to find class for map " + jsonMap.getAsJsonPrimitive("class").getAsString(), thrw);
                }

                if (!Map.class.isAssignableFrom(mapClass))
                    throw new JsonParseException("The class is not a map!");

                try {
                    map = mapClass.newInstance();
                } catch (Throwable thrw) {
                    throw new JsonParseException("Failed to construct a map of " + mapClass.getName(), thrw);
                }

                JsonArray jsonValues = jsonMap.getAsJsonArray("values");
                jsonValues.forEach(valuePair -> {
                    JsonObject valuePairJson = valuePair.getAsJsonObject();
                    Object key = null;
                    Object value = null;

                    JsonElement keyJson = valuePairJson.get("key");
                    JsonElement valueJson = valuePairJson.get("value");

                    if (keyJson.isJsonPrimitive())
                        key = parsePrimitive(keyJson.getAsJsonPrimitive());

                    else if (keyJson.isJsonObject()) {
                        try {
                            key = toObject(gson, keyJson.getAsJsonObject());
                        } catch (Throwable thrw) {
                            throw new JsonParseException("Failed to parse key value of the map!", thrw);
                        }
                    }

                    if (valueJson.isJsonPrimitive())
                        value = parsePrimitive(valueJson.getAsJsonPrimitive());

                    else if (valueJson.isJsonObject()) {
                        try {
                            value = toObject(gson, valueJson.getAsJsonObject());
                        } catch (Throwable thrw) {
                            throw new JsonParseException("Failed to parse key value of the map!", thrw);
                        }
                    }

                    map.put(key, value);
                });
                return (T) map;
            }
        };
    }

    public Object toObject(Gson gson, JsonObject object) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        String className = object.getAsJsonPrimitive("class").getAsString();
        if (isNumber(className))
            return objectToNumber(object, className);

        Class clazz = Class.forName(className);
        return gson.fromJson(object, clazz);
    }

    private boolean isNumber(Object object) {
        return isNumber(object.getClass());
    }

    private boolean isNumber(String clazz) {
        return
                clazz.contentEquals("i")
                || clazz.contentEquals("l")
                || clazz.contentEquals("f")
                || clazz.contentEquals("d")
                || clazz.contentEquals("n");
    }

    private boolean isNumber(Class clazz) {
        if (Number.class.isAssignableFrom(clazz)) return true;
        return
                clazz == int.class
                || clazz == long.class
                || clazz == double.class
                || clazz == float.class;
    }

    private Object parsePrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean())
            return primitive.getAsBoolean();

        else if (primitive.isNumber())
            return primitive.getAsNumber();

        else if (primitive.isString())
            return primitive.getAsString();

        return "none";
    }

    public JsonObject numberToObject(Object numba) {
        JsonObject object = new JsonObject();
        object.addProperty("value", numba.toString());
        object.addProperty("class", wrapNumberClass(numba));
        return object;
    }

    public String wrapNumberClass(Object numba) {
        System.out.println(numba.getClass());
        return
                numba instanceof Integer ? "i" :
                        numba instanceof Long ? "l" :
                                numba instanceof Double ? "d" :
                                        numba instanceof Float ? "f" : "n";
    }

    public Object objectToNumber(JsonObject object, String clazz) {
        String value = object.remove("value").getAsString();
        if (clazz.contentEquals("i"))
            return Integer.valueOf(value);

        else if (clazz.contentEquals("l"))
            return Long.valueOf(value);

        else if (clazz.contentEquals("d"))
            return Double.valueOf(value);

        else if (clazz.contentEquals("f"))
            return Float.valueOf(value);

        else
            throw new IllegalStateException("Failed to find number type by " + clazz + " for " + value);
    }
}
