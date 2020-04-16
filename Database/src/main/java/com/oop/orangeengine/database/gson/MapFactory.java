package com.oop.orangeengine.database.gson;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

public class MapFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!Map.class.isAssignableFrom(rawType)) return null;

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
                JsonElement jsonElement = Streams.parse(in);

                Preconditions.checkArgument(jsonElement.isJsonObject(), "Wrong type of serialized map, it must be json object!");
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                String className = jsonObject.get("class").getAsString();
                Class clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to find class by " + className + " for " + FactoryUtil.toPrettyFormat(jsonObject.toString()));
                }

                Map map;
                try {
                    Constructor constructor = clazz.getConstructor();
                    constructor.setAccessible(true);

                    map = (Map) constructor.newInstance();
                } catch (Throwable throwable) {
                    throw new IllegalStateException("Failed to find a constructor for " + clazz);
                }

                TypeAdapter<Object> delegateAdapter = gson.getDelegateAdapter(ObjectTypeAdapter.FACTORY, TypeToken.get(Object.class));

                JsonArray array = jsonObject.getAsJsonArray("values");
                for (JsonElement pairElement : array) {
                    JsonObject pairObject = pairElement.getAsJsonObject();
                    JsonElement key = pairObject.get("key");
                    JsonElement value = pairObject.get("value");

                    map.put(delegateAdapter.fromJsonTree(key), delegateAdapter.fromJsonTree(value));
                }
                return (T) map;
            }
        };
    }
}
