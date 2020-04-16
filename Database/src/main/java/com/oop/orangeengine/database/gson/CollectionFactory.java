package com.oop.orangeengine.database.gson;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;

import static com.oop.orangeengine.database.gson.FactoryUtil.readElement;

public class CollectionFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) return null;

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("class", value.getClass().getName());

                JsonArray jsonArray = new JsonArray();

                TypeAdapter<Object> delegateAdapter = gson.getDelegateAdapter(ObjectTypeAdapter.FACTORY, TypeToken.get(Object.class));
                for (Object object : (Collection) value) {
                    JsonElement jsonElement = delegateAdapter.toJsonTree(object);
                    jsonArray.add(jsonElement);
                }

                jsonObject.add("values", jsonArray);
                Streams.write(jsonObject, out);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement element = readElement(in);
                if (!element.isJsonObject()) throw new IllegalStateException("Failed to parse collection from " + FactoryUtil.toPrettyFormat(element) + " cause it's wrong type! Required JsonObject!");

                JsonObject jsonObject = element.getAsJsonObject();

                String className = jsonObject.get("class").getAsString();
                Class clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to find class by " + className + " for " + FactoryUtil.toPrettyFormat(jsonObject.toString()));
                }

                Collection collection;
                try {
                    Constructor constructor = clazz.getConstructor();
                    constructor.setAccessible(true);

                    collection = (Collection) constructor.newInstance();
                } catch (Throwable throwable) {
                    throw new IllegalStateException("Failed to find a constructor for " + clazz);
                }

                JsonArray jsonElements = jsonObject.get("values").getAsJsonArray();
                TypeAdapter<Object> delegateAdapter = gson.getDelegateAdapter(ObjectTypeAdapter.FACTORY, TypeToken.get(Object.class));
                for (JsonElement arrayElement : jsonElements) {
                    collection.add(delegateAdapter.fromJsonTree(arrayElement));
                }

                return (T) collection;
            }
        };
    }
}
