package com.oop.orangeengine.database.gson;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.util.NumberConversions;

import java.io.IOException;
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
                    pairObject.add("key", gson.toJsonTree(key));
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
                            JsonObject keyObject = keyJson.getAsJsonObject();
                            String className = keyObject.getAsJsonPrimitive("class").getAsString();
                            key = gson.fromJson(keyObject, Class.forName(className));

                            if (key instanceof LazilyParsedNumber)
                                key = numberConversion((LazilyParsedNumber) key);

                        } catch (Throwable thrw) {
                            throw new JsonParseException("Failed to parse key value of the map!", thrw);
                        }
                    }

                    if (valueJson.isJsonPrimitive())
                        value = parsePrimitive(valueJson.getAsJsonPrimitive());

                    else if (valueJson.isJsonObject()) {
                        try {
                            JsonObject valueObject = valueJson.getAsJsonObject();
                            String className = valueObject.getAsJsonPrimitive("class").getAsString();
                            value = gson.fromJson(valueObject, Class.forName(className));

                            if (value instanceof LazilyParsedNumber)
                                value = numberConversion((LazilyParsedNumber) value);

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

    private Object parsePrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean())
            return primitive.getAsBoolean();

        else if (primitive.isNumber())
            return primitive.getAsNumber();

        else if (primitive.isString())
            return primitive.getAsString();

        return "none";
    }

    private Object numberConversion(LazilyParsedNumber number) {
        return number;
    }
}
